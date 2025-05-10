package cn.dataplatform.open.web.service.impl;


import cn.dataplatform.open.common.component.OrikaMapper;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.vo.base.PageBase;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.service.UserService;
import cn.dataplatform.open.web.service.UserWorkspaceService;
import cn.dataplatform.open.web.service.WorkspaceService;
import cn.dataplatform.open.web.store.entity.User;
import cn.dataplatform.open.web.store.entity.UserWorkspace;
import cn.dataplatform.open.web.store.entity.Workspace;
import cn.dataplatform.open.web.store.mapper.UserWorkspaceMapper;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.dataplatform.open.web.vo.workspace.*;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserWorkspaceServiceImpl extends ServiceImpl<UserWorkspaceMapper, UserWorkspace> implements UserWorkspaceService {

    @Resource
    private UserWorkspaceMapper userWorkspaceMapper;
    @Lazy
    @Resource
    private UserService userService;
    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private OrikaMapper orikaMapper;


    /**
     * 我的工作空间列表
     *
     * @return r
     */
    @Override
    public List<WorkspaceData> my() {
        UserData userData = Context.getUser();
        // 如果是超级管理，返回所有的
        List<Workspace> workspaces;
        if (userData.isAdmin()) {
            workspaces = this.workspaceService.lambdaQuery()
                    .eq(Workspace::getStatus, Status.ENABLE.name())
                    .list();

        } else {
            Long userId = userData.getId();
            workspaces = userWorkspaceMapper.listWorkspaceByUserId(userId);
        }
        return workspaces.stream().map(source ->
                        orikaMapper.map(source, WorkspaceData.class))
                .collect(Collectors.toList());
    }

    /**
     * 工作空间下的用户
     *
     * @param pageRequest 分页请求
     * @return user
     */
    @Override
    public PageResult<UserData> members(PageRequest<ListWorkspaceMemberRequest> pageRequest) {
        ListWorkspaceMemberRequest query = pageRequest.getQuery();
        Long workspaceId = query.getWorkspaceId();
        PageBase page = pageRequest.getPage();
        Long total = this.userWorkspaceMapper.totalMember(workspaceId, query.getUsername(), query.getType());
        if (total == 0) {
            return new PageResult<>(Collections.emptyList(), page.getCurrent(), page.getSize(), 0L);
        }
        List<UserData> userDataList = this.userWorkspaceMapper.listMember(workspaceId, query.getUsername(), query.getType(), page);
        return new PageResult<>(userDataList, page.getCurrent(), page.getSize(), total);
    }

    /**
     * 工作空间下的不在成员
     *
     * @param pageRequest 分页请求
     * @return user
     */
    @Override
    public PageResult<UserData> notInMembers(PageRequest<NotInMembersRequest> pageRequest) {
        // 先查询出来已经存在此工作空间下的用户
        NotInMembersRequest query = pageRequest.getQuery();
        Long workspaceId = query.getWorkspaceId();
        PageBase page = pageRequest.getPage();
        List<UserWorkspace> userWorkspaces = this.lambdaQuery()
                .select(UserWorkspace::getUserId)
                .eq(UserWorkspace::getWorkspaceId, workspaceId).list();
        List<Long> userIds = userWorkspaces.stream().map(UserWorkspace::getUserId).toList();
        Page<User> userPage = this.userService.lambdaQuery()
                .eq(User::getStatus, Status.ENABLE.name())
                .notIn(CollUtil.isNotEmpty(userIds), User::getId, userIds)
                .like(StrUtil.isNotEmpty(query.getUsername()), User::getUsername, query.getUsername())
                .page(new Page<>(page.getCurrent(), page.getSize()));
        List<UserData> userDataList = userPage.getRecords().stream().map(source -> {
            UserData target = new UserData();
            this.orikaMapper.map(source, target);
            return target;
        }).collect(Collectors.toList());
        return new PageResult<>(userDataList, page.getCurrent(), page.getSize(), userPage.getTotal());
    }

    /**
     * 转移权限
     *
     * @param permissionTransferRequest p
     * @return r
     */
    @Override
    public Boolean permissionTransfer(PermissionTransferRequest permissionTransferRequest) {
        Integer type = permissionTransferRequest.getType();
        Integer workspaceId = permissionTransferRequest.getWorkspaceId();
        Integer userId = permissionTransferRequest.getUserId();
        return this.lambdaUpdate()
                .set(UserWorkspace::getIsAdmin, type)
                .eq(UserWorkspace::getUserId, userId)
                .eq(UserWorkspace::getWorkspaceId, workspaceId)
                .update();
    }

    /**
     * 删除成员
     *
     * @param deleteMemberRequest d
     * @return r
     */
    @Override
    public Boolean deleteMember(DeleteMemberRequest deleteMemberRequest) {
        Integer workspaceId = deleteMemberRequest.getWorkspaceId();
        Integer userId = deleteMemberRequest.getUserId();
        return this.lambdaUpdate()
                .eq(UserWorkspace::getWorkspaceId, workspaceId)
                .eq(UserWorkspace::getUserId, userId)
                .remove();
    }

    /**
     * 绑定成员
     *
     * @param bindMemberRequest b
     * @return r
     */
    @Override
    public Boolean bindMember(BindMemberRequest bindMemberRequest) {
        // 如果已经存在，则不再添加
        boolean exists = this.lambdaQuery()
                .eq(UserWorkspace::getUserId, bindMemberRequest.getUserId())
                .eq(UserWorkspace::getWorkspaceId, bindMemberRequest.getWorkspaceId())
                .exists();
        if (exists) {
            return true;
        }
        UserWorkspace userWorkspace = new UserWorkspace();
        userWorkspace.setUserId(bindMemberRequest.getUserId());
        userWorkspace.setWorkspaceId(bindMemberRequest.getWorkspaceId());
        userWorkspace.setCreateUserId(Context.getUser().getId());
        // 默认普通用户
        userWorkspace.setIsAdmin(0);
        return this.save(userWorkspace);
    }

}
