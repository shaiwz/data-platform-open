package cn.dataplatform.open.web.service.impl;

import cn.dataplatform.open.common.component.OrikaMapper;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.common.vo.base.PageBase;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.service.PermissionService;
import cn.dataplatform.open.web.store.entity.Permission;
import cn.dataplatform.open.web.store.mapper.PermissionMapper;
import cn.dataplatform.open.web.vo.permission.*;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.dataplatform.open.web.vo.workspace.WorkspaceData;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Resource
    private OrikaMapper orikaMapper;

    /**
     * 列表
     *
     * @param pageRequest 分页请求
     * @return user
     */
    @Override
    public PageResult<PermissionListResponse> list(PageRequest<PermissionListRequest> pageRequest) {
        PageBase page = pageRequest.getPage();
        PermissionListRequest query = Optional.ofNullable(pageRequest.getQuery()).orElse(new PermissionListRequest());
        Page<Permission> permissionPage = this.lambdaQuery()
                .like(StrUtil.isNotBlank(query.getName()), Permission::getName, query.getName())
                .like(StrUtil.isNotBlank(query.getCode()), Permission::getCode, query.getCode())
                .eq(query.getStatus() != null, Permission::getStatus, query.getStatus())
                .orderByDesc(Permission::getCreateTime)
                .page(new Page<>(page.getCurrent(), page.getSize()));
        PageResult<PermissionListResponse> pageResult = new PageResult<>();
        List<PermissionListResponse> collect = permissionPage.getRecords()
                .stream()
                .map(m -> {
                    PermissionListResponse permissionListResponse = new PermissionListResponse();
                    this.orikaMapper.map(m, permissionListResponse);
                    return permissionListResponse;
                }).collect(Collectors.toList());
        pageResult.setData(collect, permissionPage.getCurrent(), permissionPage.getSize(), permissionPage.getTotal());
        return pageResult;
    }

    /**
     * 详情
     *
     * @param id id
     * @return r
     */
    @Override
    public PermissionDetailResponse detail(Long id) {
        WorkspaceData workspace = Context.getWorkspace();
        Permission permission = this.getById(id);
        if (permission == null) {
            throw new ApiException("权限不存在");
        }
        PermissionDetailResponse permissionDetailResponse = new PermissionDetailResponse();
        this.orikaMapper.map(permission, permissionDetailResponse);
        return permissionDetailResponse;
    }

    /**
     * 添加
     *
     * @param permissionAddRequest 请求
     * @return r
     */
    @Override
    public Boolean add(PermissionAddRequest permissionAddRequest) {
        String code = permissionAddRequest.getCode();
        // 编码是否存在
        if (this.lambdaQuery()
                .eq(Permission::getCode, code)
                .exists()) {
            throw new ApiException("权限编码已存在");
        }
        UserData user = Context.getUser();
        Permission permission = new Permission();
        this.orikaMapper.map(permissionAddRequest, permission);
        permission.setCreateUserId(user.getId());
        this.save(permission);
        return true;
    }

    /**
     * 更新
     *
     * @param permissionUpdateRequest 请求
     * @return r
     */
    @Override
    public Boolean update(PermissionUpdateRequest permissionUpdateRequest) {
        Long id = permissionUpdateRequest.getId();
        // 编码是否存在
        if (this.lambdaQuery()
                .eq(Permission::getCode, permissionUpdateRequest.getCode())
                .ne(Permission::getId, id)
                .exists()) {
            throw new ApiException("权限编码已存在");
        }
        Permission permission = this.getById(id);
        if (permission == null) {
            throw new ApiException("权限不存在");
        }
        this.orikaMapper.map(permissionUpdateRequest, permission);
        this.updateById(permission);
        return true;
    }

    /**
     * 删除
     *
     * @param id id
     * @return r
     */
    @Override
    public Boolean delete(Long id) {
        Permission permission = this.getById(id);
        if (permission == null) {
            throw new ApiException("权限不存在");
        }
        this.removeById(id);
        return true;
    }

}
