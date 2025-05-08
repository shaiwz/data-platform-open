package cn.dataplatform.open.common.aspect;

import cn.dataplatform.open.common.constant.Constant;
import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/22
 * @since 1.0.0
 */
@Aspect
@Component
public class ScheduledAspect {

    private static final String TASK_LOCK_PREFIX = "dp:scheduled:lock:";

    @Resource
    private RedissonClient redissonClient;

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MDC.put(Constant.REQUEST_ID, UUID.fastUUID().toString(true));
        String methodName = joinPoint.getSignature().getName();
        RLock lock = this.redissonClient.getLock(TASK_LOCK_PREFIX.concat(methodName));
        try {
            if (!lock.tryLock(0L, 60L, TimeUnit.SECONDS)) {
                return null;
            }
            // 执行定时任务
            return joinPoint.proceed();
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            MDC.clear();
        }
    }

}
