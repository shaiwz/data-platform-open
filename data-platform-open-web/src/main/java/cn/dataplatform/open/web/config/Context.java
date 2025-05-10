package cn.dataplatform.open.web.config;

import cn.dataplatform.open.web.vo.user.UserData;
import cn.dataplatform.open.web.vo.workspace.WorkspaceData;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
public class Context {

    /**
     * 本次请求的用户信息
     */
    private static final ThreadLocal<UserData> USER = new ThreadLocal<>();

    /**
     * 本次请求的工作空间
     */
    private static final ThreadLocal<WorkspaceData> WORKSPACE = new ThreadLocal<>();


    /**
     * 获取当前登录用户
     *
     * @return UserData
     */
    public static UserData getUser() {
        return Context.USER.get();
    }

    /**
     * 设置当前登录用户
     *
     * @param userData 用户信息
     */
    public static void setUser(UserData userData) {
        Context.USER.set(userData);
    }

    /**
     * 清空所有信息
     */
    public static void clearAll() {
        Context.USER.remove();
        Context.WORKSPACE.remove();
    }

    /**
     * 获取当前工作空间
     *
     * @return WorkspaceData
     */
    public static WorkspaceData getWorkspace() {
        return Context.WORKSPACE.get();
    }

    /**
     * 设置当前工作空间
     *
     * @param workspaceData 工作空间
     */
    public static void setWorkspace(WorkspaceData workspaceData) {
        Context.WORKSPACE.set(workspaceData);
    }


    /**
     * 移除用户信息
     */
    public static void removeUser() {
        Context.USER.remove();
    }

}
