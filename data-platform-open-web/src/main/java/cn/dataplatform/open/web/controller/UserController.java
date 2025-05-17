package cn.dataplatform.open.web.controller;

import cn.dataplatform.open.common.vo.base.IdRequest;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.common.vo.base.PlainResult;
import cn.dataplatform.open.web.annotation.Auth;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.service.UserService;
import cn.dataplatform.open.web.vo.user.*;
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
@RequestMapping("user")
public class UserController {

    @Resource
    private UserService userService;


    /**
     * 获取登录人信息
     *
     * @return user
     */
    @PostMapping("getUserInfo")
    public PlainResult<UserData> getUserInfo() {
        PlainResult<UserData> plainResult = new PlainResult<>();
        plainResult.setData(Context.getUser());
        return plainResult;
    }

    /**
     * 列表
     *
     * @param pageRequest 分页请求
     * @return user
     */
    @Auth("system:user:list")
    @PostMapping("list")
    public PageResult<UserListResponse> list(@RequestBody @Valid PageRequest<UserListRequest> pageRequest) {
        return this.userService.list(pageRequest);
    }

    /**
     * 详情
     *
     * @param idRequest id
     * @return r
     */
    @Auth("system:user:detail")
    @PostMapping("detail")
    public PlainResult<UserDetailResponse> detail(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.userService.detail(idRequest.getId()));
    }

    /**
     * 添加
     *
     * @param userAddRequest 请求
     * @return r
     */
    @Auth("system:user:add")
    @PostMapping("add")
    public PlainResult<Boolean> add(@RequestBody @Valid UserAddRequest userAddRequest) {
        return new PlainResult<>(this.userService.add(userAddRequest));
    }

    /**
     * 更新
     *
     * @param userUpdateRequest 请求
     * @return r
     */
    @PostMapping("update")
    public PlainResult<Boolean> update(@RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        return new PlainResult<>(this.userService.update(userUpdateRequest));
    }

    /**
     * 删除
     *
     * @param idRequest id
     * @return r
     */
    @Auth("system:user:delete")
    @PostMapping("delete")
    public PlainResult<Boolean> delete(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.userService.delete(idRequest.getId()));
    }

    /**
     * 管理员给用户重置密码
     *
     * @param request 请求
     * @return r
     */
    @Auth("system:user:reset-password")
    @PostMapping("resetPassword")
    public PlainResult<Boolean> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return new PlainResult<>(this.userService.resetPassword(request));
    }

    /**
     * 用户修改自己的密码
     *
     * @param request 请求
     * @return r
     */
    @PostMapping("changePassword")
    public PlainResult<Boolean> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        return new PlainResult<>(this.userService.changePassword(request));
    }

}
