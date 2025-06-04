package cn.dataplatform.open.web.service.impl;

import cn.dataplatform.open.common.component.OrikaMapper;
import cn.dataplatform.open.common.vo.base.PageBase;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.enums.OperationLogAction;
import cn.dataplatform.open.web.enums.OperationLogFunction;
import cn.dataplatform.open.web.service.OperationLogService;
import cn.dataplatform.open.web.service.UserService;
import cn.dataplatform.open.web.store.entity.OperationLog;
import cn.dataplatform.open.web.store.entity.User;
import cn.dataplatform.open.web.store.mapper.OperationLogMapper;
import cn.dataplatform.open.web.vo.operation.log.OperationLogDetailResponse;
import cn.dataplatform.open.web.vo.operation.log.OperationLogListRequest;
import cn.dataplatform.open.web.vo.operation.log.OperationLogListResponse;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.dataplatform.open.web.vo.workspace.WorkspaceData;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/9
 * @since 1.0.0
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Resource
    private OrikaMapper orikaMapper;
    @Resource
    private UserService userService;

    /**
     * 操作日志列表
     *
     * @param pageRequest p
     * @return r
     */
    @Override
    public PageResult<OperationLogListResponse> list(PageRequest<OperationLogListRequest> pageRequest) {
        PageBase page = pageRequest.getPage();
        OperationLogListRequest query = pageRequest.getQuery();
        WorkspaceData workspace = Context.getWorkspace();
        Page<OperationLog> paged = this.lambdaQuery()
                .like(StrUtil.isNotBlank(query.getUsername()), OperationLog::getUsername, query.getUsername())
                .eq(StrUtil.isNotBlank(query.getRequestId()), OperationLog::getRequestId, query.getRequestId())
                .eq(query.getUserId() != null, OperationLog::getUserId, query.getUserId())
                // time
                .ge(query.getStartCreateTime() != null, OperationLog::getCreateTime, query.getStartCreateTime())
                .le(query.getEndCreateTime() != null, OperationLog::getCreateTime, query.getEndCreateTime())
                .eq(OperationLog::getWorkspaceCode, workspace.getCode())
                .eq(StrUtil.isNotBlank(query.getFunction()), OperationLog::getFunction, query.getFunction())
                .eq(StrUtil.isNotBlank(query.getAction()), OperationLog::getAction, query.getAction())
                .eq(StrUtil.isNotBlank(query.getStatus()), OperationLog::getStatus, query.getStatus())
                .orderByDesc(OperationLog::getCreateTime)
                .page(new Page<>(page.getCurrent(), page.getSize()));
        PageResult<OperationLogListResponse> pageResult = new PageResult<>();
        List<OperationLog> records = paged.getRecords();
        if (records.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), paged.getCurrent(), paged.getSize(), paged.getTotal());
        }
        Set<Long> userIds = records.stream().map(OperationLog::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = this.userService.getAllUserMapByIds(userIds);
        List<OperationLogListResponse> collect = records.stream()
                .map(m -> {
                    OperationLogListResponse listResponse = new OperationLogListResponse();
                    this.orikaMapper.map(m, listResponse);
                    // 枚举转换
                    listResponse.setAction(OperationLogAction.valueOf(m.getAction()).getName());
                    listResponse.setFunction(OperationLogFunction.valueOf(m.getFunction()).getName());
                    // 用户
                    User u = userMap.get(m.getUserId());
                    if (u != null) {
                        UserData userData = this.orikaMapper.map(u, UserData.class);
                        listResponse.setUser(userData);
                    }
                    return listResponse;
                }).collect(Collectors.toList());
        pageResult.setData(collect, paged.getCurrent(), paged.getSize(), paged.getTotal());
        return pageResult;
    }

    /**
     * 删除
     *
     * @param id r
     * @return r
     */
    @Override
    public Boolean delete(Long id) {
        return this.removeById(id);
    }

    /**
     * 详情
     *
     * @param id r
     * @return r
     */
    @Override
    public OperationLogDetailResponse detail(Long id) {
        OperationLog operationLog = this.getById(id);
        if (operationLog == null) {
            throw new RuntimeException("操作日志不存在");
        }
        OperationLogDetailResponse detailResponse = new OperationLogDetailResponse();
        this.orikaMapper.map(operationLog, detailResponse);
        detailResponse.setAction(OperationLogAction.valueOf(operationLog.getAction()).getName());
        detailResponse.setFunction(OperationLogFunction.valueOf(operationLog.getFunction()).getName());
        return detailResponse;
    }

}
