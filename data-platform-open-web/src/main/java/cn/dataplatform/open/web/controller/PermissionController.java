package cn.dataplatform.open.web.controller;

import cn.dataplatform.open.common.vo.base.IdRequest;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.common.vo.base.PlainResult;
import cn.dataplatform.open.web.annotation.Auth;
import cn.dataplatform.open.web.service.PermissionService;
import cn.dataplatform.open.web.vo.permission.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author DaoDao
 */
@RestController
@RequestMapping("permission")
public class PermissionController {


    @Resource
    private PermissionService permissionService;


    /**
     * 列表
     *
     * @param pageRequest 分页请求
     * @return user
     */
    @PostMapping("list")
    public PageResult<PermissionListResponse> list(@RequestBody @Valid PageRequest<PermissionListRequest> pageRequest) {
        return this.permissionService.list(pageRequest);
    }

    /**
     * 详情
     *
     * @param idRequest id
     * @return r
     */
    @Auth("system:permission:detail")
    @PostMapping("detail")
    public PlainResult<PermissionDetailResponse> detail(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.permissionService.detail(idRequest.getId()));
    }

    /**
     * 添加
     *
     * @param permissionAddRequest 请求
     * @return r
     */
    @Auth("system:permission:add")
    @PostMapping("add")
    public PlainResult<Boolean> add(@RequestBody @Valid PermissionAddRequest permissionAddRequest) {
        return new PlainResult<>(this.permissionService.add(permissionAddRequest));
    }

    /**
     * 更新
     *
     * @param permissionUpdateRequest 请求
     * @return r
     */
    @Auth("system:permission:update")
    @PostMapping("update")
    public PlainResult<Boolean> update(@RequestBody @Valid PermissionUpdateRequest permissionUpdateRequest) {
        return new PlainResult<>(this.permissionService.update(permissionUpdateRequest));
    }

    /**
     * 删除
     *
     * @param idRequest id
     * @return r
     */
    @Auth("system:permission:delete")
    @PostMapping("delete")
    public PlainResult<Boolean> delete(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.permissionService.delete(idRequest.getId()));
    }

}
