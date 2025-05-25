package cn.dataplatform.open.web.service.impl;

import cn.dataplatform.open.common.component.OrikaMapper;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.service.PermissionService;
import cn.dataplatform.open.web.service.RolePermissionService;
import cn.dataplatform.open.web.store.entity.Permission;
import cn.dataplatform.open.web.store.entity.RolePermission;
import cn.dataplatform.open.web.store.mapper.RolePermissionMapper;
import cn.dataplatform.open.web.vo.permission.MyPermissionResponse;
import cn.dataplatform.open.web.vo.permission.PermissionListResponse;
import cn.dataplatform.open.web.vo.permission.RolePermissionUpsertRequest;
import cn.dataplatform.open.web.vo.user.UserData;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author DaoDao
 */
@Slf4j
@Service
public class RolePermissionServiceImpl extends
        ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

    @Resource
    private RolePermissionMapper rolePermissionMapper;
    @Resource
    private PermissionService permissionService;
    @Resource
    private OrikaMapper orikaMapper;

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public List<PermissionListResponse> list(Long roleId) {
        List<Permission> roleList = this.rolePermissionMapper.listPermissionByRoleId(roleId);
        return roleList.stream()
                .map(source -> {
                    PermissionListResponse target = new PermissionListResponse();
                    this.orikaMapper.map(source, target);
                    return target;
                }).collect(Collectors.toList());
    }


    /**
     * 查询当前用户的权限列表
     *
     * @return 权限列表
     */
    @Override
    public List<MyPermissionResponse> my() {
        UserData userData = Context.getUser();
        List<Permission> permissions;
        if (userData.isAdmin()) {
            permissions = this.permissionService.lambdaQuery()
                    .eq(Permission::getStatus, Status.ENABLE.name())
                    .list();
        } else {
            Long userId = userData.getId();
            permissions = this.rolePermissionMapper.listPermissionByUserId(userId);
        }
        return permissions.stream().map(source -> {
            MyPermissionResponse target = new MyPermissionResponse();
            this.orikaMapper.map(source, target);
            return target;
        }).collect(Collectors.toList());
    }

    /**
     * 更新权限
     *
     * @return true
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean upsert(RolePermissionUpsertRequest rolePermissionUpsertRequest) {
        UserData userData = Context.getUser();
        Long createUserId = userData.getId();
        Long roleId = rolePermissionUpsertRequest.getRoleId();
        List<Long> permissionIds = rolePermissionUpsertRequest.getPermissionIds();
        // 删除原有权限
        this.rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId));
        // 添加新权限
        ArrayList<RolePermission> rolePermissions = new ArrayList<>();
        permissionIds.forEach(permissionId -> {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermission.setCreateUserId(createUserId);
            rolePermissions.add(rolePermission);
        });
        this.saveBatch(rolePermissions);
        return true;
    }

}
