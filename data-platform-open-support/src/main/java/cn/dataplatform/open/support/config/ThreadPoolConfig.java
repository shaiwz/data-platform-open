/*
 * ============================================================================
 *
 *                    数海文舟 (DATA PLATFORM) 版权所有 © 2025
 *
 *       本软件受著作权法和国际版权条约保护，本软件受著作权法和国际版权条约保护，未经明确书面授权，任何单位或个人不得对本软件进行复制、修改、分发、
 *       逆向工程、商业用途等任何形式的非法使用。违者将面临人民币100万元的
 *       法定罚款及可能的法律追责。
 *
 *       举报侵权行为可获得实际罚款金额40%的现金奖励。
 *       举报渠道：
 *           - 法务邮箱：dingqw@shaiwz.com，761945125@qq.com
 *
 *       COPYRIGHT (C) 2025 dingqianwen COMPANY. ALL RIGHTS RESERVED.
 *
 * ============================================================================
 */
package cn.dataplatform.open.support.config;

import cn.dataplatform.open.common.config.ThreadPoolTaskExecutorBeanPostProcessor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * 线程池
 *
 * @author 丁乾文
 * @date 2021/6/17
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    public static final String VIRTUAL_EXECUTOR = "virtualExecutor";

    @Resource
    @Lazy
    private ThreadPoolTaskExecutorBeanPostProcessor.TaskDecoratorProxy taskDecoratorProxy;

    /**
     * 虚拟线程池
     *
     * @return 虚拟线程池
     */
    @Bean(name = VIRTUAL_EXECUTOR)
    public AsyncTaskExecutor virtualExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setTaskDecorator(this.taskDecoratorProxy.getTaskDecorator(null));
        // 开启虚拟线程模式
        executor.setVirtualThreads(true);
        return executor;
    }

}
