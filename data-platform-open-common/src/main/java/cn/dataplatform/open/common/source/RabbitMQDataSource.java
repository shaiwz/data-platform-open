package cn.dataplatform.open.common.source;

import cn.dataplatform.open.common.enums.DataSourceType;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * RabbitMQDataSource
 *
 * @author dqw
 * @since 1.0.0
 */
@Slf4j
@Data
public class RabbitMQDataSource implements Source {

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

    @Setter(value = AccessLevel.NONE)
    private volatile ConnectionFactory connectionFactory;
    @Setter(value = AccessLevel.NONE)
    private volatile RabbitTemplate rabbitTemplate;

    /**
     * 获取连接工厂
     *
     * @return 连接工厂
     */
    public ConnectionFactory getConnectionFactory() {
        if (connectionFactory == null) {
            synchronized (this) {
                if (connectionFactory == null) {
                    log.info("初始化RabbitMQ连接工厂:" + this.url);
                    CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
                    connectionFactory.setUri(url);
                    // connectionFactory.setPort(5672);
                    connectionFactory.setUsername(username);
                    connectionFactory.setPassword(password);
                    connectionFactory.setVirtualHost("/");
                    this.connectionFactory = connectionFactory;
                    log.info("初始化RabbitMQ连接工厂完成");
                }
            }
        }
        return connectionFactory;
    }

    /**
     * 获取RabbitTemplate
     */
    public RabbitTemplate getRabbitTemplate() {
        if (rabbitTemplate == null) {
            synchronized (this) {
                if (rabbitTemplate == null) {
                    ConnectionFactory connectionFactory = this.getConnectionFactory();
                    this.rabbitTemplate = new RabbitTemplate(connectionFactory);
                }
            }
        }
        return rabbitTemplate;
    }

    /**
     * 获取连接
     */
    @SneakyThrows
    public Connection getConnection() {
        // 如果数据源为空则初始化
        return this.getConnectionFactory().createConnection();
    }

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
        return DataSourceType.RABBIT_MQ;
    }

    @Override
    public Boolean isEnableHealth() {
        return isEnableHealth;
    }

    /**
     * 健康检查
     *
     * @return true健康
     */
    @Override
    public Boolean health() throws Exception {
        try (Connection connection = this.getConnection();) {
            return connection.isOpen();
        }
    }

    /**
     * 关闭数据源
     */
    @Override
    public void close() {
        log.info("关闭RabbitMQ数据源:" + this.code);
        // 关闭所有监听
        this.rabbitTemplate.stop();
        if (this.connectionFactory instanceof CachingConnectionFactory) {
            ((CachingConnectionFactory) this.connectionFactory).destroy();
        }
        this.rabbitTemplate = null;
        this.connectionFactory = null;
        log.info("关闭RabbitMQ数据源完成");
    }

}