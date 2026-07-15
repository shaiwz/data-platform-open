package cn.dataplatform.open.common.config;

import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.server.ServerManager;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/1
 * @since 1.0.0
 */
@Configuration
public class MetricsConfig {

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Resource
    private ServerManager serverManager;

    /**
     * 设置全局tag
     *
     * @return MeterRegistryCustomizer
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
                // 应用名称 data-platform-flow
                Constant.APPLICATION, this.applicationName,
                // 服务实例ID localhost:8080
                Constant.INSTANCE, this.serverManager.instanceId()
        );
    }

}
