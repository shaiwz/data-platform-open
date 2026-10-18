package cn.dataplatform.open.common.source;


import cn.dataplatform.open.common.enums.DataSourceType;
import cn.dataplatform.open.common.exception.DataSourceException;
import cn.dataplatform.open.common.util.IoUtils;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * KafkaDataSource
 *
 * @author dqw
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class KafkaDataSource extends Source {

    @Setter(AccessLevel.NONE)
    private volatile AdminClient adminClient;
    @Setter(AccessLevel.NONE)
    private volatile KafkaProducer<String, String> kafkaProducer;

    /**
     * 数据源类型
     *
     * @return 类型
     */
    @Override
    public DataSourceType type() {
        return DataSourceType.KAFKA;
    }

    /**
     * 获取管理客户端
     *
     * @return 管理客户端
     */
    public AdminClient getAdminClient() {
        if (this.adminClient == null) {
            synchronized (this) {
                if (this.adminClient == null) {
                    Properties props = new Properties();
                    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.url);
                    // 默认rebootstrap
                    props.put(AdminClientConfig.METADATA_RECOVERY_STRATEGY_CONFIG, AdminClientConfig.DEFAULT_METADATA_RECOVERY_STRATEGY);
                    // 请求超时时间，默认30000ms
                    props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);
                    // 重试间隔，默认100ms
                    props.put(AdminClientConfig.RETRY_BACKOFF_MS_CONFIG, 5000);
                    // 两次重试之间的最大等待时间（默认 1000ms）
                    props.put(AdminClientConfig.RETRY_BACKOFF_MAX_MS_CONFIG, 60_000);
                    // 设置 SASL 认证相关配置
                    if (StrUtil.isNotBlank(this.username) && StrUtil.isNotBlank(this.password)) {
                        props.put("security.protocol", "SASL_SSL");
                        props.put("sasl.mechanism", "PLAIN");
                        props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                                "username=\"" + this.username + "\" " +
                                "password=\"" + this.password + "\";");
                    }
                    // 添加额外的自定义属性
                    if (this.properties != null) {
                        props.putAll(this.properties);
                    }
                    this.adminClient = AdminClient.create(props);
                }
            }
        }
        return this.adminClient;
    }

    /**
     * 获取Kafka生产者
     *
     * @return Kafka生产者
     */
    public KafkaProducer<String, String> getKafkaProducer() {
        if (this.kafkaProducer == null) {
            synchronized (this) {
                if (this.kafkaProducer == null) {
                    Properties props = new Properties();
                    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.url);
                    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                    if (StrUtil.isNotBlank(this.username)) {
                        props.put("security.inter.broker.protocol", "SASL_PLAINTEXT");
                        props.put("sasl.enabled.mechanisms", "PLAIN");
                        props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + this.username
                                + "\" password=\"" + this.password + "\";");
                    }
                    props.put("acks", "all");
                    if (this.properties != null) {
                        props.putAll(this.properties);
                    }
                    this.kafkaProducer = new KafkaProducer<>(props);
                }
            }
        }
        return this.kafkaProducer;
    }

    /**
     * 健康检查，失败抛出异常
     *
     * @throws Exception 检查失败原因
     */
    @Override
    public void health() throws Exception {
        // 设置描述集群的选项
        DescribeClusterOptions options = new DescribeClusterOptions().timeoutMs(5000);
        DescribeClusterResult result = this.getAdminClient().describeCluster(options);
        // 获取集群ID（验证连接）
        String clusterId = result.clusterId().get(5, TimeUnit.SECONDS);
        // 获取节点信息（验证至少有一个broker可用）
        Collection<Node> nodes = result.nodes().get(5, TimeUnit.SECONDS);
        if (nodes.isEmpty() || clusterId == null) {
            throw new DataSourceException("Kafka 集群不可用");
        }
    }

    /**
     * 关闭
     */
    @Override
    public synchronized void close() {
        log.info("关闭 Kafka 数据源: {}({})", this.code, this.name);
        if (this.adminClient != null) {
            IoUtils.close(this.adminClient);
            this.adminClient = null;
        }
        if (this.kafkaProducer != null) {
            IoUtils.close(this.kafkaProducer);
            this.kafkaProducer = null;
        }
    }

}