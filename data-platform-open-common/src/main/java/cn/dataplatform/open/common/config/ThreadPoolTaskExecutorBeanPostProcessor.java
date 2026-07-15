package cn.dataplatform.open.common.config;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2021/1/29
 * @since 1.0.0
 */
@Slf4j
@Component
public class ThreadPoolTaskExecutorBeanPostProcessor implements BeanPostProcessor {

    private static final Field TASK_DECORATOR_FIELD = FieldUtils.getDeclaredField(ThreadPoolTaskExecutor.class,
            "taskDecorator", true);

    @Resource
    @Lazy
    private TaskDecoratorProxy taskDecoratorProxy;

    /**
     * 在初始化之前进行线程池装饰器的替换
     *
     * @param bean     bean
     * @param beanName bean名称
     * @return bean
     * @throws BeansException 异常
     */
    @SneakyThrows
    @Override
    @NonNull
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof ThreadPoolTaskExecutor threadPoolTaskExecutor) {
            TaskDecorator taskDecorator = (TaskDecorator) TASK_DECORATOR_FIELD.get(threadPoolTaskExecutor);
            TaskDecorator tracerTaskDecorator = this.taskDecoratorProxy.getTaskDecorator(taskDecorator);
            threadPoolTaskExecutor.setTaskDecorator(tracerTaskDecorator);
            return threadPoolTaskExecutor;
        }
        return bean;
    }


    public interface TaskDecoratorProxy {

        /**
         * 获取装饰器
         *
         * @param taskDecorator 装饰器
         * @return 装饰器
         */
        TaskDecorator getTaskDecorator(TaskDecorator taskDecorator);

    }

    @Component
    public static class DefaultTaskDecoratorProxy implements TaskDecoratorProxy {

        @Override
        public TaskDecorator getTaskDecorator(TaskDecorator delegate) {
            return runnable -> {
                Map<String, String> context = MDC.getCopyOfContextMap();
                Runnable finalRunnable = Objects.nonNull(delegate) ? delegate.decorate(runnable) : runnable;
                return () -> {
                    try {
                        if (context != null) {
                            MDC.setContextMap(context);
                        }
                        finalRunnable.run();
                    } finally {
                        MDC.clear();
                    }
                };
            };
        }
    }

}
