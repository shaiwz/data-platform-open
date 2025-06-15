package cn.dataplatform.open.web.aspect;

import cn.dataplatform.open.common.enums.RedisKey;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.common.util.HttpServletUtils;
import cn.dataplatform.open.common.util.IPUtils;
import cn.dataplatform.open.web.annotation.RateLimit;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.vo.user.UserData;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * 接口级别限流,依赖于redis
 *
 * @author 丁乾文
 * @date 2021/6/17
 * @since 1.0.0
 */
@Component
@Aspect
@Slf4j
@Order(-8)
public class RateLimitAspect {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流环绕通知
     *
     * @param joinPoint joinPoint
     * @param rateLimit rateLimit
     * @return Object
     * @throws Throwable Throwable
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        UserData userData = Context.getUser();
        if ( // 登录无用户
                userData != null && userData.isAdmin()) {
            // 管理员随意访问
            return joinPoint.proceed();
        }
        String value = switch (rateLimit.type()) {
            case IP -> IPUtils.getRequestIp();
            case URL -> HttpServletUtils.getRequest().getRequestURI();
            case USER -> {
                if (userData == null) {
                    throw new ApiException("选择用户方式" +
                            "限流,但是没有获取到用户信息");
                } else {
                    yield userData.getId().toString();
                }
            }
            case URL_IP -> {
                HttpServletRequest request = HttpServletUtils.getRequest();
                yield request.getRequestURI() + request.getRemoteAddr();
            }
        };
        String key = RedisKey.RATE_LIMIT.build(value);
        log.info("执行限流拦截器,限制类型:{},key:{}", rateLimit.type(), key);
        this.executor(key, rateLimit);
        return joinPoint.proceed();
    }

    /**
     * 限流执行器
     *
     * @param key       redis key
     * @param rateLimit 速率参数
     */
    private void executor(String key, RateLimit rateLimit) {
        // 限制时间间隔
        long refreshInterval = rateLimit.refreshInterval();
        // 限制时间间隔内可用次数
        long limit = rateLimit.limit();
        // 时间单位
        ChronoUnit chronoUnit = rateLimit.chronoUnit();
        RRateLimiter rateLimiter = this.redissonClient.getRateLimiter(key);
        // 初始化RateLimiter的状态,并将配置存储到Redis服务器
        if (!rateLimiter.isExists()) {
            boolean trySetRate = rateLimiter.trySetRate(RateType.OVERALL, limit, Duration.of(refreshInterval, chronoUnit));
            log.info("初始化RateLimiter的状态:{}", trySetRate);
            // 限流数据保存10天
            rateLimiter.expire(Duration.of(10, ChronoUnit.DAYS));
        }
        if (!rateLimiter.tryAcquire()) {
            throw new ApiException("你访问过于频繁,请稍后重试!");
        }
    }

}
