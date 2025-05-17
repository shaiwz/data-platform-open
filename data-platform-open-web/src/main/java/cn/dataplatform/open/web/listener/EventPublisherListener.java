package cn.dataplatform.open.web.listener;

import cn.dataplatform.open.common.body.*;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.event.*;
import cn.dataplatform.open.web.config.RabbitConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 事件发布监听器,消息结偶,后面方便更换mq,或者使用spring cloud stream
 */
@Slf4j
@Component
public class EventPublisherListener {

    @Lazy
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 事物结束后,发送mq消息通知需要加载或者移出的数据流
     *
     * @param dataFlowEvent 数据流事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = DataFlowEvent.class)
    public void dataFlowEvent(DataFlowEvent dataFlowEvent) {
        log.info("发送数据流消息:" + dataFlowEvent.getSource());
        DataFlowMessageBody flowMessageBody = dataFlowEvent.getSource();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader(Constant.REQUEST_ID, MDC.get(Constant.REQUEST_ID));
        Message message = this.rabbitTemplate.getMessageConverter().toMessage(flowMessageBody, messageProperties);
        this.rabbitTemplate.convertAndSend(RabbitConfig.FLOW_EXCHANGE, "", message);
    }


    /**
     * 事物结束后,发送mq消息通知需要加载或者移出的数据源
     *
     * @param dataSourceEvent 数据源事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = DataSourceEvent.class)
    public void dataSourceEvent(DataSourceEvent dataSourceEvent) {
        log.info("发送数据源消息:" + dataSourceEvent.getSource());
        DataSourceMessageBody dataSourceMessageBody = dataSourceEvent.getSource();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader(Constant.REQUEST_ID, MDC.get(Constant.REQUEST_ID));
        Message message = this.rabbitTemplate.getMessageConverter().toMessage(dataSourceMessageBody, messageProperties);
        this.rabbitTemplate.convertAndSend(RabbitConfig.SOURCE_EXCHANGE, "", message);
    }


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

}