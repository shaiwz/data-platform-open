package cn.dataplatform.open.common.enums.flow;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/30
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum IdempotentType {

    /**
     * RabbitMQ
     */
    RABBITMQ("RabbitMQ"),

    /**
     * RocketMQ
     */
    ROCKETMQ("RocketMQ"),

    /**
     * Kafka
     */
    KAFKA("Kafka"),
    ;

    private final String code;

}
