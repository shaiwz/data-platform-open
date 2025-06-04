package cn.dataplatform.open.common.config;

import cn.dataplatform.open.common.server.ServerManager;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Resource;
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

    private static final String APPLICATION = "application";
    private static final String INSTANCE_ID = "instanceId";

    @Resource
    private ServerManager serverManager;

    /**
     * 设置全局tag
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
                // 应用名称 data-platform-flow
                APPLICATION, this.serverManager.getApplicationName(),
                // 服务实例ID localhost:8080
                INSTANCE_ID, this.serverManager.instanceId()
        );
    }

}
