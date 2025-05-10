package cn.dataplatform.open.web.service.impl;


import cn.dataplatform.open.common.body.DataSourceMessageBody;
import cn.dataplatform.open.common.component.OrikaMapper;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.event.DataSourceEvent;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.common.vo.base.PageBase;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.service.WorkspaceService;
import cn.dataplatform.open.web.service.datasource.DataSourceService;
import cn.dataplatform.open.web.service.flow.DataFlowPublishService;
import cn.dataplatform.open.web.store.entity.DataFlowPublish;
import cn.dataplatform.open.web.store.entity.DataSource;
import cn.dataplatform.open.web.store.entity.Workspace;
import cn.dataplatform.open.web.store.mapper.WorkspaceMapper;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.dataplatform.open.web.vo.workspace.*;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkspaceServiceImpl extends ServiceImpl<WorkspaceMapper, Workspace> implements WorkspaceService {

    @Resource
    private OrikaMapper orikaMapper;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Lazy
    @Resource
    private DataSourceService dataSourceService;
    @Lazy
    @Resource
    private DataFlowPublishService dataFlowPublishService;

    /**
     * 列表
     *
     * @param pageRequest 分页请求
     * @return user
     */
    @Override
    public PageResult<WorkspaceListResponse> list(PageRequest<WorkspaceListRequest> pageRequest) {
        PageBase page = pageRequest.getPage();
        WorkspaceListRequest query = Optional.ofNullable(pageRequest.getQuery()).orElse(new WorkspaceListRequest());
        Page<Workspace> workspacePage = this.lambdaQuery()
                .like(StrUtil.isNotBlank(query.getName()), Workspace::getName, query.getName())
                .eq(StrUtil.isNotBlank(query.getCode()), Workspace::getCode, query.getCode())
                .eq(StrUtil.isNotBlank(query.getStatus()), Workspace::getStatus, query.getStatus())
                .page(new Page<>(page.getCurrent(), page.getSize()));
        PageResult<WorkspaceListResponse> pageResult = new PageResult<>();
        List<WorkspaceListResponse> collect = workspacePage.getRecords()
                .stream()
                .map(m -> {
                    WorkspaceListResponse workspaceListResponse = new WorkspaceListResponse();
                    this.orikaMapper.map(m, workspaceListResponse);
                    return workspaceListResponse;
                }).collect(Collectors.toList());
        pageResult.setData(collect, workspacePage.getCurrent(), workspacePage.getSize(), workspacePage.getTotal());
        return pageResult;
    }

    /**
     * 详情
     *
     * @param id id
     * @return r
     */
    @Override
    public WorkspaceDetailResponse detail(Long id) {
        Workspace workspace = this.getById(id);
        if (workspace == null) {
            throw new ApiException("工作空间不存在");
        }
        WorkspaceDetailResponse workspaceDetailResponse = new WorkspaceDetailResponse();
        this.orikaMapper.map(workspace, workspaceDetailResponse);
        return workspaceDetailResponse;
    }

    /**
     * 添加
     *
     * @param workspaceAddRequest 请求
     * @return r
     */
    @Override
    public Boolean add(WorkspaceAddRequest workspaceAddRequest) {
        UserData user = Context.getUser();
        // 只有超级管理员可以新增工作空间，方便维护，不然用户可以随意创建工作空间，导致混乱
        if (!user.isAdmin()) {
            throw new ApiException("只有超级管理员可以新增工作空间");
        }
        // 名称是否存在
        if (this.lambdaQuery()
                .eq(Workspace::getName, workspaceAddRequest.getName())
                .exists()) {
            throw new ApiException("工作空间名称已存在");
        }
        Workspace workspace = new Workspace();
        this.orikaMapper.map(workspaceAddRequest, workspace);
        workspace.setSecret(UUID.fastUUID().toString(true));
        workspace.setCreateUserId(user.getId());
        this.save(workspace);
        return true;
    }

    /**
     * 更新
     *
     * @param workspaceUpdateRequest 请求
     * @return r
     */
    @Override
    public Boolean update(WorkspaceUpdateRequest workspaceUpdateRequest) {
        // 名称是否存在
        if (this.lambdaQuery()
                .eq(Workspace::getName, workspaceUpdateRequest.getName())
                .ne(Workspace::getId, workspaceUpdateRequest.getId())
                .exists()) {
            throw new ApiException("工作空间名称已存在");
        }
        Long id = workspaceUpdateRequest.getId();
        Workspace workspace = this.getById(id);
        this.orikaMapper.map(workspaceUpdateRequest, workspace);
        this.updateById(workspace);
        return true;
    }

    /**
     * 删除
     *
     * @param id id
     * @return r
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean delete(Long id) {
        Workspace workspace = this.getById(id);
        if (workspace == null) {
            throw new ApiException("工作空间不存在");
        }
        // 以下必须一个一个手动关闭，确认都可以关停，防止误操作
        if (this.dataFlowPublishService.lambdaQuery()
                .eq(DataFlowPublish::getWorkspaceCode, workspace.getCode())
                .eq(DataFlowPublish::getStatus, Status.ENABLE.name())
                .exists()) {
            throw new ApiException("工作空间下有数据流任务正在运行中，轻先停止");
        }
        // 停用工作空间下的数据源
        // 查询出来
        List<DataSource> dataSources = this.dataSourceService.lambdaQuery()
                .eq(DataSource::getWorkspaceCode, workspace.getCode())
                .eq(DataSource::getStatus, Status.ENABLE.name())
                .list();
        if (CollUtil.isNotEmpty(dataSources)) {
            for (DataSource dataSource : dataSources) {
                this.dataSourceService.removeById(dataSource.getId());
                // 移除数据源
                DataSourceMessageBody messageBody = new DataSourceMessageBody();
                messageBody.setType(DataSourceMessageBody.Type.REMOVE);
                messageBody.setId(dataSource.getId());
                messageBody.setWorkspaceCode(dataSource.getWorkspaceCode());
                this.applicationEventPublisher.publishEvent(new DataSourceEvent(messageBody));
            }
        }
        this.removeById(id);
        return true;
    }

}
