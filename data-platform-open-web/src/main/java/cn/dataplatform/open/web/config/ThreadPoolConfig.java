package cn.dataplatform.open.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author dingqianwen
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    /**
     * 默认线程池
     */
    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // 线程池维护线程的最少数量
        taskExecutor.setCorePoolSize(5);
        // 线程池维护线程的最大数量
        taskExecutor.setMaxPoolSize(50);
        // 设置线程的名称
        taskExecutor.setThreadNamePrefix("default_");
        // 线程池所使用的缓冲队列
        taskExecutor.setQueueCapacity(100);
        // 线程池维护线程所允许的空闲时间
        taskExecutor.setKeepAliveSeconds(60);
        taskExecutor.setAllowCoreThreadTimeOut(true);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(60);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        return taskExecutor;
    }

}
