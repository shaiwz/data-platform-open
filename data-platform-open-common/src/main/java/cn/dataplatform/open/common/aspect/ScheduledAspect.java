package cn.dataplatform.common.aspect;

import cn.dataplatform.open.common.annotation.ScheduledGlobalLock;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.enums.RedisKey;
import cn.dataplatform.open.common.server.ServerManager;
import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/22
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class ScheduledAspect {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ServerManager serverManager;

    /**
     * 拦截所有带有 @Scheduled 注解的方法 执行时增加requestId 以及判断是否需要分布式锁
     *
     * @param joinPoint j
     * @return r
     * @throws Throwable t
     */
    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MDC.put(Constant.REQUEST_ID, UUID.fastUUID().toString(true));
        // 检查方法是否有 @ScheduledGlobalLock 注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ScheduledGlobalLock scheduledGlobalLock = method.getAnnotation(ScheduledGlobalLock.class);
        RLock lock = null;
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        String lockKey = className + ":" + methodName;
        try {
            if (scheduledGlobalLock != null) {
                lock = this.redissonClient.getLock(RedisKey.SCHEDULED_LOCK.build(lockKey));
                if (!lock.tryLock(scheduledGlobalLock.waitTime(),
                        scheduledGlobalLock.leaseTime(), scheduledGlobalLock.unit())) {
                    log.info("Scheduled任务未获取到锁: {}, 当前实例: {}", lockKey, this.serverManager.instanceId());
                    // 获取锁失败，直接返回
                    return null;
                }
            }
            // 执行定时任务
            log.info("Scheduled任务获取到锁: {}, 当前实例: {}", lockKey, this.serverManager.instanceId());
            return joinPoint.proceed();
        } finally {
            // 如果有锁，并且当前线程持有锁，则释放
            if (lock != null && lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            MDC.clear();
        }
    }

}
