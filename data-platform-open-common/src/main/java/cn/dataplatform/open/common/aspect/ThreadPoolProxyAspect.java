package cn.dataplatform.open.common.aspect;

import cn.dataplatform.open.common.config.ThreadPoolTaskExecutorBeanPostProcessor;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;


/**
 * 〈ThreadAspect〉
 *
 * @author 丁乾文
 * @date 2023/9/5 20:06
 * @since 1.0.0
 */
@Aspect
@Component
public class ThreadPoolProxyAspect {

    @Resource
    private ThreadPoolTaskExecutorBeanPostProcessor.TaskDecoratorProxy taskDecoratorProxy;

    /**
     * 线程池执行时,包装Runnable
     *
     * @param joinPoint j
     * @param runnable  r
     * @return r
     * @throws Throwable t
     */
    @Around(value = "execution(* java.util.concurrent.ExecutorService.execute(Runnable)) && args(runnable)")
    public Object around(ProceedingJoinPoint joinPoint, Runnable runnable) throws Throwable {
        TaskDecorator taskDecorator = this.taskDecoratorProxy.getTaskDecorator(null);
        Runnable decorate = taskDecorator.decorate(runnable);
        return joinPoint.proceed(new Object[]{decorate});
    }

}
