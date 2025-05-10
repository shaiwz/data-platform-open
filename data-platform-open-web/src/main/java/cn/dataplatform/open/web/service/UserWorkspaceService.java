package cn.dataplatform.open.web.service;

import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.store.entity.UserWorkspace;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.dataplatform.open.web.vo.workspace.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author DaoDao
 */
public interface UserWorkspaceService extends IService<UserWorkspace> {

    /**
     * 我的工作空间列表
     *
     * @return r
     */
    List<WorkspaceData> my();

    /**
     * 工作空间下的用户
     *
     * @param pageRequest 分页请求
     * @return user
     */
    PageResult<UserData> members(PageRequest<ListWorkspaceMemberRequest> pageRequest);

    /**
     * 工作空间下的不在成员
     *
     * @param pageRequest 分页请求
     * @return user
     */
    PageResult<UserData> notInMembers(PageRequest<NotInMembersRequest> pageRequest);

    /**
     * 转移权限
     *
     * @param permissionTransferRequest p
     * @return r
     */
    Boolean permissionTransfer(PermissionTransferRequest permissionTransferRequest);

    /**
     * 删除成员
     *
     * @param deleteMemberRequest d
     * @return r
     */
    Boolean deleteMember(DeleteMemberRequest deleteMemberRequest);

    /**
     * 绑定成员
     *
     * @param bindMemberRequest b
     * @return r
     */
    Boolean bindMember(BindMemberRequest bindMemberRequest);

}
