package cn.dataplatform.open.web.interceptor;


import cn.dataplatform.open.common.component.OrikaMapper;
import cn.dataplatform.open.common.enums.ErrorCode;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.service.UserWorkspaceService;
import cn.dataplatform.open.web.service.WorkspaceService;
import cn.dataplatform.open.web.store.entity.UserWorkspace;
import cn.dataplatform.open.web.store.entity.Workspace;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.dataplatform.open.web.vo.workspace.WorkspaceData;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.util.Objects;

/**
 * 工作空间权限校验,并把当前工作空间设置到上下文中
 *
 * @author dingqianwen
 * @date 2025/1/19
 * @since 1.0.0
 */
@Slf4j
@Component
public class WorkspaceInterceptor implements AsyncHandlerInterceptor {

    /**
     * http header 中的工作空间
     */
    public static final String X_WORKSPACE = "X-Workspace";

    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private UserWorkspaceService userWorkspaceService;
    @Resource
    private OrikaMapper orikaMapper;

    /**
     * 校验工作空间权限
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return 是否通过
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String xWorkspace = request.getHeader(WorkspaceInterceptor.X_WORKSPACE);
        if (StrUtil.isEmpty(xWorkspace)) {
            throw new ApiException(ErrorCode.DP_10011041);
        }
        Long workspaceId = Long.valueOf(xWorkspace);
        log.info("当前工作空间:{}", workspaceId);
        Workspace workspace = this.workspaceService.lambdaQuery().eq(Workspace::getId, workspaceId).one();
        if (workspace == null) {
            throw new ApiException(ErrorCode.DP_10011040);
        }
        // 部分场景可能不需要登录状态,也就是没有用户信息
        UserData userData = Context.getUser();
        // 1为工作空间管理员
        Integer isWorkspaceAdmin = 0;
        if (userData != null &&
                // 不是管理员才需要校验
                !Objects.equals(userData.getUsername(), UserData.ADMIN)) {
            UserWorkspace userWorkspace = this.userWorkspaceService.lambdaQuery()
                    .eq(UserWorkspace::getUserId, userData.getId())
                    .eq(UserWorkspace::getWorkspaceId, workspaceId).one();
            if (userWorkspace == null) {
                throw new ApiException("用户没有权限访问此工作空间");
            }
            isWorkspaceAdmin = userWorkspace.getIsAdmin();
        }
        WorkspaceData data = new WorkspaceData();
        this.orikaMapper.map(workspace, data);
        data.setIsWorkspaceAdmin(isWorkspaceAdmin == 1);
        Context.setWorkspace(data);
        return true;
    }


}
