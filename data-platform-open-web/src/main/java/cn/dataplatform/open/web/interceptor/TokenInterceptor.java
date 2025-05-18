package cn.dataplatform.open.web.interceptor;


import cn.dataplatform.open.common.enums.ErrorCode;
import cn.dataplatform.open.common.enums.RedisKey;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.web.annotation.NoLogin;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.util.JWTUtils;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/19
 * @since 1.0.0
 */
@Slf4j
@Component
public class TokenInterceptor implements AsyncHandlerInterceptor {
    public static final String AUTHORIZATION = "Authorization";

    @Value("${dp.auth.token.keep-time:3600000}")
    public Long tokenKeepTime;

    @Resource
    private RedissonClient redissonClient;

    /**
     * Token拦截器
     *
     * @param request  request
     * @param response response
     * @param handler  handler
     * @return 是否通过
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 如果Controller有NoAuth注解的方法,不需要验证权限
        if (this.isHaveAccess(handler)) {
            log.debug("此{}接口不需要认证权限", request.getRequestURI());
            return true;
        }
        //获取Header中的token
        String token = request.getHeader(AUTHORIZATION);
        if (StrUtil.isNullOrUndefined(token)) {
            throw new ApiException(ErrorCode.DP_10010004);
        }
        try {
            // 对token进行验证
            JWTUtils.verifyToken(token);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.DP_10011039);
        }
        // 从redis获取到用户信息保存到本地
        RBucket<UserData> bucket = this.redissonClient.getBucket(RedisKey.TOKEN.build(token));
        // 获取redis中存的用户信息
        UserData userData = bucket.get();
        if (userData == null) {
            throw new ApiException(ErrorCode.DP_99990402);
        }
        Context.setUser(userData);
        // 续期
        bucket.expire(Duration.of(this.tokenKeepTime, ChronoUnit.MILLIS));
        log.debug("Token验证通过,User:{}", userData);
        return true;
    }


    /**
     * 是否需要验证权限
     *
     * @param handler handler
     * @return 返回true时不鉴权
     */
    private boolean isHaveAccess(Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return false;
        }
        Method method = handlerMethod.getMethod();
        return method.isAnnotationPresent(NoLogin.class);
    }


}
