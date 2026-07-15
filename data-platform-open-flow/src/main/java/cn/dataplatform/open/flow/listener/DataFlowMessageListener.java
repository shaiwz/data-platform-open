package cn.dataplatform.open.flow.listener;

import cn.dataplatform.open.common.alarm.scene.ServerMessageExceptionScene;
import cn.dataplatform.open.common.body.DataFlowMessageBody;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.event.AlarmSceneEvent;
import cn.dataplatform.open.common.server.ServerManager;
import cn.dataplatform.open.flow.config.RabbitConfig;
import cn.dataplatform.open.flow.service.DataFlowPublishService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/11
 * @since 1.0.0
 */
@Slf4j
@Component
public class DataFlowMessageListener {

    @Resource
    private DataFlowPublishService dataFlowPublishService;
    @Resource
    private ServerManager serverManager;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 监听数据流消息,包括更新、加载、删除等事件
     * <p>
     * 注意: 该监听器是FANOUT类型的,会接收所有发送到RabbitConfig.FLOW_EXCHANGE的消息
     *
     * @param messaging 消息体
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue,
            exchange = @Exchange(value = RabbitConfig.FLOW_EXCHANGE, type = ExchangeTypes.FANOUT)
    ))
    public void onMessage(Message<DataFlowMessageBody> messaging) {
        String requestId = messaging.getHeaders().get(Constant.REQUEST_ID, String.class);
        MDC.put(Constant.REQUEST_ID, requestId);
        DataFlowMessageBody dataFlowMessageBody = messaging.getPayload();
        try {
            log.info("数据流消息:{}", dataFlowMessageBody);
            DataFlowMessageBody.Type type = dataFlowMessageBody.getType();
            switch (type) {
                case UPDATE:
                case LOAD:
                    // 服务如果不在线,例如主动下线,不在处理load事件
                    if (!this.serverManager.status()) {
                        return;
                    }
                    this.dataFlowPublishService.load(dataFlowMessageBody.getId());
                    break;
                case REMOVE:
                    this.dataFlowPublishService.stop(dataFlowMessageBody.getId());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
            ServerMessageExceptionScene scene = new ServerMessageExceptionScene(e);
            scene.setConsumerClassName(this.getClass().getName());
            scene.setConsumerMethodName(methodName);
            this.applicationEventPublisher.publishEvent(new AlarmSceneEvent(dataFlowMessageBody.getWorkspaceCode(), scene));
            throw e;
        } finally {
            MDC.clear();
        }
    }

}
