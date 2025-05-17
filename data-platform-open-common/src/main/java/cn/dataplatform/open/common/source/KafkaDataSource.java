package cn.dataplatform.open.common.source;


import cn.dataplatform.open.common.enums.DataSourceType;
import cn.dataplatform.open.common.vo.flow.KeyValue;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * KafkaDataSource
 *
 * @author dqw
 * @since 1.0.0
 */
@Slf4j
@Data
public class KafkaDataSource implements Source {

    private String code;
    private String name;
    /**
     * 连接信息
     */
    @NotBlank
    private String url;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private Boolean isEnableHealth;

    private List<KeyValue> properties;

    private volatile AdminClient adminClient;

    /**
     * 获取数据源编码
     *
     * @return 数据源编码
     */
    @Override
    public String code() {
        return code;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public DataSourceType type() {
        return DataSourceType.KAFKA;
    }

    /**
     * 是否启用健康检查
     *
     * @return true启用
     */
    @Override
    public Boolean isEnableHealth() {
        return isEnableHealth;
    }

    /**
     * 获取管理客户端
     *
     * @return 管理客户端
     */
    public AdminClient getAdminClient() {
        if (adminClient == null) {
            synchronized (this) {
                if (adminClient == null) {
                    Properties props = new Properties();
                    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, url);
                    // 设置 SASL 认证相关配置
                    if (StrUtil.isNotBlank(username) && StrUtil.isNotBlank(password)) {
                        props.put("security.protocol", "SASL_SSL");
                        props.put("sasl.mechanism", "PLAIN");
                        props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                                "username=\"" + username + "\" " +
                                "password=\"" + password + "\";");
                    }
                    // 添加额外的自定义属性
                    if (properties != null) {
                        for (KeyValue keyValue : properties) {
                            props.put(keyValue.getKey(), keyValue.getValue());
                        }
                    }
                    adminClient = AdminClient.create(props);
                }
            }
        }
        return adminClient;
    }

    /**
     * 健康检查
     *
     * @return true健康
     */
    @Override
    public Boolean health() throws Exception {
        // 设置描述集群的选项
        DescribeClusterOptions options = new DescribeClusterOptions().timeoutMs(5000);
        DescribeClusterResult result = this.getAdminClient().describeCluster(options);
        // 获取集群ID（验证连接）
        String clusterId = result.clusterId().get(5, TimeUnit.SECONDS);
        // 获取节点信息（验证至少有一个broker可用）
        Collection<Node> nodes = result.nodes().get(5, TimeUnit.SECONDS);
        return !nodes.isEmpty() && clusterId != null;
    }

    /**
     * 关闭
     */
    @Override
    public void close() {
        log.info("关闭Kafka数据源:" + this.code);
        if (adminClient != null) {
            adminClient.close();
            adminClient = null;
        }
    }

}