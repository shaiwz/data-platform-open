package cn.dataplatform.open.flow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

/**
 * 〈一句话功能简述〉<br>
 * 〈发布与订阅〉
 *
 * @author 丁乾文
 * @date 2021/6/17
 * @since 1.0.0
 */
@Slf4j
@Component
public class RabbitConfig {

    public static final String ALARM_QUEUE = "dp-alarm-queue";
    public static final String ALARM_SCENE_QUEUE = "dp-alarm-scene-queue";

    public static final String FLOW_QUEUE = "dp-data-flow-queue";
    public static final String FLOW_EXCHANGE = "dp-data-flow-exchange";

    public static final String FLOW_DISPATCH_QUEUE = "dp-data-flow-dispatch-queue";
    public static final String FLOW_DISPATCH_EXCHANGE = "dp-data-flow-dispatch-exchange";

    /**
     * 启动引擎中某个组件的队列
     */
    public static final String FLOW_COMPONENT_QUEUE = "dp-data-flow-component-queue";
    public static final String FLOW_COMPONENT_EXCHANGE = "dp-data-flow-component-exchange";

    public static final String SOURCE_QUEUE = "dp-data-source-queue";
    public static final String SOURCE_EXCHANGE = "dp-data-source-exchange";


    /**
     * 当配置了rabbitmq配置时启用
     */
    @ConditionalOnProperty("spring.rabbitmq.host")
    @EnableRabbit
    @Component
    @Import(RabbitAutoConfiguration.class)
    public static class RabbitConfiguration {

        public RabbitConfiguration() {
            log.info("load rabbit");
        }


        /**
         * 使用json传输,即使没有实现序列化接口也可以
         *
         * @return MessageConverter
         */
        @Bean
        public MessageConverter messageConverter() {
            return new Jackson2JsonMessageConverter();
        }

    }


    @ConditionalOnMissingBean(RabbitConfiguration.class)
    @Component
    public static class RabbitNoConfigurationWarning {

        /**
         * 未配置Rabbit将导致数据流不能正常发布
         */
        public RabbitNoConfigurationWarning() {
            if (log.isWarnEnabled()) {
                log.warn("If Rabbit is not configured, the rules will not be published normally");
            }
        }

    }

    @Bean
    public Queue alarmQueue() {
        return new Queue(ALARM_QUEUE);
    }

    @Bean
    public Queue alarmSceneQueue() {
        return new Queue(ALARM_SCENE_QUEUE, true);
    }

    @Bean
    public FanoutExchange flowExchange() {
        return new FanoutExchange(FLOW_EXCHANGE);
    }

    @Bean
    public Queue flowQueue() {
        return new Queue(FLOW_QUEUE);
    }

    @Bean
    public Binding flowBindingExchangeMessage(@Qualifier("flowQueue") Queue flowQueue,
                                              @Qualifier("flowExchange") FanoutExchange flowExchange) {
        return BindingBuilder.bind(flowQueue).to(flowExchange);
    }


    @Bean
    public FanoutExchange flowComponentExchange() {
        return new FanoutExchange(FLOW_COMPONENT_EXCHANGE);
    }

    @Bean
    public Queue flowComponentQueue() {
        return new Queue(FLOW_COMPONENT_QUEUE);
    }

    @Bean
    public Binding flowComponentBindingExchangeMessage(@Qualifier("flowComponentQueue") Queue flowComponentQueue,
                                                       @Qualifier("flowComponentExchange") FanoutExchange flowComponentExchange) {
        return BindingBuilder.bind(flowComponentQueue).to(flowComponentExchange);
    }

    @Bean
    public FanoutExchange sourceExchange() {
        return new FanoutExchange(SOURCE_EXCHANGE);
    }

    @Bean
    public Queue sourceQueue() {
        return new Queue(SOURCE_QUEUE);
    }

    @Bean
    public Binding sourceBindingExchangeMessage(@Qualifier("sourceQueue") Queue sourceQueue,
                                                @Qualifier("sourceExchange") FanoutExchange sourceExchange) {
        return BindingBuilder.bind(sourceQueue).to(sourceExchange);
    }

    @Bean
    public FanoutExchange flowDispatchExchange() {
        return new FanoutExchange(FLOW_DISPATCH_EXCHANGE);
    }

    @Bean
    public Queue flowDispatchQueue() {
        return new Queue(FLOW_DISPATCH_QUEUE);
    }

    @Bean
    public Binding flowDispatchBindingExchangeMessage(@Qualifier("flowDispatchQueue") Queue flowDispatchQueue,
                                                      @Qualifier("flowDispatchExchange") FanoutExchange flowDispatchExchange) {
        return BindingBuilder.bind(flowDispatchQueue).to(flowDispatchExchange);
    }


}
