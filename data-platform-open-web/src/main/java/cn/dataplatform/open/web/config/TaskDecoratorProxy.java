package cn.dataplatform.open.web.config;

import cn.dataplatform.open.common.config.ThreadPoolTaskExecutorBeanPostProcessor;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.dataplatform.open.web.vo.workspace.WorkspaceData;
import org.slf4j.MDC;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;


/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/6/14
 * @since 1.0.0
 */
@Primary
@Component
public class TaskDecoratorProxy implements ThreadPoolTaskExecutorBeanPostProcessor.TaskDecoratorProxy {

    /**
     * 父子线程传递上下文
     *
     * @param delegate 任务装饰器
     * @return 包装后的任务装饰器
     */
    @Override
    public TaskDecorator getTaskDecorator(TaskDecorator delegate) {
        return runnable -> {
            Map<String, String> context = MDC.getCopyOfContextMap();
            Runnable finalRunnable = Objects.nonNull(delegate) ? delegate.decorate(runnable) : runnable;
            UserData user = Context.getUser();
            WorkspaceData workspace = Context.getWorkspace();
            return () -> {
                try {
                    if (context != null) {
                        MDC.setContextMap(context);
                    }
                    if (user != null) {
                        Context.setUser(user);
                    }
                    if (workspace != null) {
                        Context.setWorkspace(workspace);
                    }
                    finalRunnable.run();
                } finally {
                    MDC.clear();
                    Context.clearAll();
                }
            };
        };
    }


}
