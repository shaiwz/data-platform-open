package cn.dataplatform.open.web.controller;

import cn.dataplatform.open.common.vo.base.IdRequest;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.common.vo.base.PlainResult;
import cn.dataplatform.open.web.annotation.Auth;
import cn.dataplatform.open.web.service.RoleService;
import cn.dataplatform.open.web.vo.role.*;
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
@RequestMapping("role")
public class RoleController {

    @Resource
    private RoleService roleService;


    /**
     * 列表
     *
     * @param pageRequest 分页请求
     * @return user
     */
    @PostMapping("list")
    public PageResult<RoleListResponse> list(@RequestBody @Valid PageRequest<RoleListRequest> pageRequest) {
        return this.roleService.list(pageRequest);
    }

    /**
     * 详情
     *
     * @param idRequest id
     * @return r
     */
    @Auth("system:role:detail")
    @PostMapping("detail")
    public PlainResult<RoleDetailResponse> detail(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.roleService.detail(idRequest.getId()));
    }

    /**
     * 添加
     *
     * @param roleAddRequest 请求
     * @return r
     */
    @Auth("system:role:add")
    @PostMapping("add")
    public PlainResult<Boolean> add(@RequestBody @Valid RoleAddRequest roleAddRequest) {
        return new PlainResult<>(this.roleService.add(roleAddRequest));
    }

    /**
     * 更新
     *
     * @param roleUpdateRequest 请求
     * @return r
     */
    @Auth("system:role:update")
    @PostMapping("update")
    public PlainResult<Boolean> update(@RequestBody @Valid RoleUpdateRequest roleUpdateRequest) {
        return new PlainResult<>(this.roleService.update(roleUpdateRequest));
    }

    /**
     * 删除
     *
     * @param idRequest id
     * @return r
     */
    @Auth("system:role:delete")
    @PostMapping("delete")
    public PlainResult<Boolean> delete(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.roleService.delete(idRequest.getId()));
    }

}
