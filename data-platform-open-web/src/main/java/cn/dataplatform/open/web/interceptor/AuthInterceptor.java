package cn.dataplatform.open.web.interceptor;


import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.web.annotation.Auth;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.store.mapper.RolePermissionMapper;
import cn.dataplatform.open.web.vo.user.UserData;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/19
 * @since 1.0.0
 */
@Component
public class AuthInterceptor implements AsyncHandlerInterceptor {

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    /**
     * 权限校验
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return 是否通过
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 菜单接口权限在这里做
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            // 不需要校验
            return true;
        }
        // 如果是管理员,不需要验证
        UserData userData = Context.getUser();
        if (UserData.ADMIN.equals(userData.getUsername())) {
            return true;
        }
        Method method = handlerMethod.getMethod();
        Auth auth = method.getAnnotation(Auth.class);
        if (auth == null) {
            // 不需要验证权限
            return true;
        }
        String[] value = auth.value();
        // 判断用户是否有此接口权限,如果指定了value则按照指定的值,例如user:update,否则按照接口地址
        Boolean checkUserPermission = this.rolePermissionMapper.hasAnyPermission(userData.getId(),
                Arrays.asList(value));
        if (!checkUserPermission) {
            throw new ApiException("无权限访问");
        }
        return true;
    }

}