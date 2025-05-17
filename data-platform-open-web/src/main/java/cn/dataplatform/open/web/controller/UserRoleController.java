package cn.dataplatform.open.web.controller;

import cn.dataplatform.open.common.vo.base.IdRequest;
import cn.dataplatform.open.common.vo.base.PlainResult;
import cn.dataplatform.open.web.service.UserRoleService;
import cn.dataplatform.open.web.vo.role.RoleDetailResponse;
import cn.dataplatform.open.web.vo.role.UserRoleUpsertRequest;
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
@RequestMapping("user/role")
public class UserRoleController {

    @Resource
    private UserRoleService userRoleService;

    /**
     * 用户的角色列表
     *
     * @param idRequest id
     * @return r
     */
    @PostMapping("list")
    public PlainResult<List<RoleDetailResponse>> list(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.userRoleService.list(idRequest.getId()));
    }

    /**
     * 我的角色列表
     *
     * @return r
     */
    @PostMapping("my")
    public PlainResult<List<RoleDetailResponse>> my() {
        return new PlainResult<>(this.userRoleService.my());
    }

    /**
     * 更新用户角色
     *
     * @param userRoleUpsertRequest 请求
     * @return r
     */
    @PostMapping("upsert")
    public PlainResult<Boolean> upsert(@RequestBody @Valid UserRoleUpsertRequest userRoleUpsertRequest) {
        return new PlainResult<>(this.userRoleService.upsert(userRoleUpsertRequest));
    }

}
