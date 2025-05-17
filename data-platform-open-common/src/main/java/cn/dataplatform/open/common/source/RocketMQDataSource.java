package cn.dataplatform.open.common.source;

import cn.dataplatform.open.common.enums.DataSourceType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

import java.io.IOException;

@Slf4j
@Data
public class RocketMQDataSource implements Source {

    private String code;
    private String name;

    @NotBlank
    private String url;

    private Boolean isEnableHealth;

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
        return DataSourceType.ROCKET_MQ;
    }

    @Override
    public Boolean isEnableHealth() {
        return isEnableHealth;
    }

    /**
     * 获取生产者
     *
     * @return producer
     */
    public DefaultMQProducer getProducer() {
        log.info("初始化 RocketMQ Producer : {}", this.code);
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(url);
        log.info("初始化 RocketMQ Producer 完成");
        return producer;
    }

    /**
     * 获取消费者
     *
     * @return consumer
     */
    public DefaultMQPushConsumer getConsumer() {
        log.info("初始化 RocketMQ Consumer : {}", this.code);
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
        consumer.setNamesrvAddr(url);
        log.info("初始化 RocketMQ Consumer 完成");
        return consumer;
    }

    /**
     * 健康检查
     *
     * @return true/false
     */
    @Override
    public Boolean health() throws Exception {
        DefaultMQProducer producer = this.getProducer();
        try {
            producer.start();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        } finally {
            producer.shutdown();
        }
    }

    /**
     * 关闭资源
     */
    @Override
    public void close() throws IOException {
    }

}
