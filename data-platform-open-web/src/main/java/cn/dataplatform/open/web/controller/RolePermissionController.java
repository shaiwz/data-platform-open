package cn.dataplatform.open.web.controller;


import cn.dataplatform.open.common.vo.base.IdRequest;
import cn.dataplatform.open.common.vo.base.PlainResult;
import cn.dataplatform.open.web.annotation.Auth;
import cn.dataplatform.open.web.service.RolePermissionService;
import cn.dataplatform.open.web.vo.permission.MyPermissionResponse;
import cn.dataplatform.open.web.vo.permission.PermissionListResponse;
import cn.dataplatform.open.web.vo.permission.RolePermissionUpsertRequest;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author DaoDao
 */
@RestController
@RequestMapping("role/permission")
public class RolePermissionController {


    @Resource
    private RolePermissionService rolePermissionService;

    /**
     * 角色的权限列表
     *
     * @param idRequest id
     * @return r
     */
    @PostMapping("list")
    public PlainResult<List<PermissionListResponse>> list(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.rolePermissionService.list(idRequest.getId()));
    }

    /**
     * 我的权限列表
     *
     * @return r
     */
    @PostMapping("my")
    public PlainResult<List<MyPermissionResponse>> my() {
        return new PlainResult<>(this.rolePermissionService.my());
    }

    /**
     * 更新角色权限
     *
     * @param rolePermissionUpsertRequest 请求
     * @return r
     */
    @Auth("system:role:auth")
    @PostMapping("upsert")
    public PlainResult<Boolean> upsert(@RequestBody @Valid RolePermissionUpsertRequest rolePermissionUpsertRequest) {
        return new PlainResult<>(this.rolePermissionService.upsert(rolePermissionUpsertRequest));
    }

}
