package cn.dataplatform.open.web.service;

import cn.dataplatform.open.web.store.entity.RolePermission;
import cn.dataplatform.open.web.vo.permission.MyPermissionResponse;
import cn.dataplatform.open.web.vo.permission.PermissionListResponse;
import cn.dataplatform.open.web.vo.permission.RolePermissionUpsertRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author DaoDao
 */
public interface RolePermissionService extends IService<RolePermission> {

    /**
     * 角色的权限列表
     *
     * @return r
     */
    List<PermissionListResponse> list(Long roleId);

    /**
     * 我的权限列表
     *
     * @return r
     */
    List<MyPermissionResponse> my();

    /**
     * 更新角色权限
     *
     * @param rolePermissionUpsertRequest 请求
     * @return r
     */
    Boolean upsert(RolePermissionUpsertRequest rolePermissionUpsertRequest);
}
