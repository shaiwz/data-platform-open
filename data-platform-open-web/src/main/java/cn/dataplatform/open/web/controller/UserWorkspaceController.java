package cn.dataplatform.open.web.controller;

import cn.dataplatform.open.common.vo.base.*;
import cn.dataplatform.open.web.annotation.Auth;
import cn.dataplatform.open.web.service.UserWorkspaceService;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.dataplatform.open.web.vo.workspace.*;
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
@RequestMapping("user/workspace")
public class UserWorkspaceController {

    @Resource
    private UserWorkspaceService userWorkspaceService;

    /**
     * 我的工作空间列表
     *
     * @return r
     */
    @PostMapping("my")
    public PlainResult<List<WorkspaceData>> my() {
        return new PlainResult<>(this.userWorkspaceService.my());
    }

    /**
     * 工作空间下的用户
     *
     * @param pageRequest 分页请求
     * @return user
     */
    @PostMapping("members")
    public PageResult<UserData> members(@RequestBody @Valid PageRequest<ListWorkspaceMemberRequest> pageRequest) {
        return this.userWorkspaceService.members(pageRequest);
    }

    /**
     * 工作空间下的不在成员
     *
     * @param pageRequest 分页请求
     * @return user
     */
    @PostMapping("notInMembers")
    public PageResult<UserData> notInMembers(@RequestBody @Valid PageRequest<NotInMembersRequest> pageRequest) {
        return this.userWorkspaceService.notInMembers(pageRequest);
    }


    /**
     * 转移权限
     *
     * @param permissionTransferRequest p
     * @return r
     */
    @Auth("system:workspace:user-manage")
    @PostMapping("permissionTransfer")
    public BaseResult permissionTransfer(@RequestBody @Valid PermissionTransferRequest permissionTransferRequest) {
        PlainResult<Boolean> plainResult = new PlainResult<>();
        plainResult.setData(userWorkspaceService.permissionTransfer(permissionTransferRequest));
        return plainResult;
    }

    /**
     * 删除成员
     *
     * @param deleteMemberRequest d
     * @return r
     */
    @Auth("system:workspace:user-manage")
    @PostMapping("deleteMember")
    public BaseResult deleteMember(@RequestBody @Valid DeleteMemberRequest deleteMemberRequest) {
        PlainResult<Boolean> plainResult = new PlainResult<>();
        plainResult.setData(userWorkspaceService.deleteMember(deleteMemberRequest));
        return plainResult;
    }

    /**
     * 绑定成员
     *
     * @param bindMemberRequest b
     * @return r
     */
    @Auth("system:workspace:user-manage")
    @PostMapping("bindMember")
    public BaseResult bindMember(@RequestBody @Valid BindMemberRequest bindMemberRequest) {
        PlainResult<Boolean> plainResult = new PlainResult<>();
        plainResult.setData(userWorkspaceService.bindMember(bindMemberRequest));
        return plainResult;
    }


}
