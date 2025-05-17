package cn.dataplatform.open.flow.listener;

import cn.dataplatform.open.common.body.*;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.event.*;
import cn.dataplatform.open.flow.config.RabbitConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/23
 * @since 1.0.0
 */
@Slf4j
@Component
public class EventPublisherListener {

    @Lazy
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 异常场景事件监听
     *
     * @param alarmSceneEvent 异常场景事件
     */
    @EventListener(classes = AlarmSceneEvent.class)
    public void sceneEvent(AlarmSceneEvent alarmSceneEvent) {
        log.info("发送告警场景消息:" + alarmSceneEvent.getSource());
        AlarmSceneMessageBody alarmSceneMessageBody = alarmSceneEvent.getSource();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader(Constant.REQUEST_ID, MDC.get(Constant.REQUEST_ID));
        Message message = this.rabbitTemplate.getMessageConverter().toMessage(alarmSceneMessageBody, messageProperties);
        this.rabbitTemplate.convertAndSend(RabbitConfig.ALARM_SCENE_QUEUE, message);
    }

    /**
     * 异常事件监听
     *
     * @param alarmEvent 异常事件
     */
    @EventListener(classes = AlarmEvent.class)
    public void alarmEvent(AlarmEvent alarmEvent) {
        log.info("发送异常消息:" + alarmEvent.getSource());
        AlarmMessageBody alarmMessageBody = alarmEvent.getSource();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader(Constant.REQUEST_ID, MDC.get(Constant.REQUEST_ID));
        Message message = this.rabbitTemplate.getMessageConverter().toMessage(alarmMessageBody, messageProperties);
        this.rabbitTemplate.convertAndSend(RabbitConfig.ALARM_QUEUE, message);
    }


    /**
     * 发送数据流消息
     *
     * @param dataFlowEvent 数据流事件
     */
    @EventListener(classes = DataFlowEvent.class)
    public void dataFlowEvent(DataFlowEvent dataFlowEvent) {
        log.info("发送数据流消息:" + dataFlowEvent.getSource());
        DataFlowMessageBody flowMessageBody = dataFlowEvent.getSource();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader(Constant.REQUEST_ID, MDC.get(Constant.REQUEST_ID));
        Message message = this.rabbitTemplate.getMessageConverter().toMessage(flowMessageBody, messageProperties);
        this.rabbitTemplate.convertAndSend(RabbitConfig.FLOW_EXCHANGE, "", message);
    }


    /**
     * 发送数据流组件启动消息
     *
     * @param dataFlowComponentEvent 数据流组件启动事件
     */
    @EventListener(classes = DataFlowComponentEvent.class)
    public void dataFlowComponentStartEvent(DataFlowComponentEvent dataFlowComponentEvent) {
        log.info("发送数据流组件启动消息:" + dataFlowComponentEvent.getSource());
        DataFlowComponentMessageBody flowComponentMessageBody = dataFlowComponentEvent.getSource();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader(Constant.REQUEST_ID, MDC.get(Constant.REQUEST_ID));
        Message message = this.rabbitTemplate.getMessageConverter().toMessage(flowComponentMessageBody, messageProperties);
        this.rabbitTemplate.convertAndSend(RabbitConfig.FLOW_COMPONENT_EXCHANGE, "", message);
    }

    /**
     * 数据流调度事件
     *
     * @param dataFlowDispatchEvent 数据流组件启动事件
     */
    @EventListener(classes = DataFlowDispatchEvent.class)
    public void dataFlowDispatchEvent(DataFlowDispatchEvent dataFlowDispatchEvent) {
        log.info("发送数据流调度消息:" + dataFlowDispatchEvent.getSource());
        DataFlowDispatchMessageBody dispatchMessageBody = dataFlowDispatchEvent.getSource();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader(Constant.REQUEST_ID, MDC.get(Constant.REQUEST_ID));
        Message message = this.rabbitTemplate.getMessageConverter().toMessage(dispatchMessageBody, messageProperties);
        this.rabbitTemplate.convertAndSend(RabbitConfig.FLOW_DISPATCH_EXCHANGE, "", message);
    }

}
