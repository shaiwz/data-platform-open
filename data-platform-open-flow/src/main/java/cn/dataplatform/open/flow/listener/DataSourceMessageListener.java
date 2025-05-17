package cn.dataplatform.open.flow.listener;

import cn.dataplatform.open.common.alarm.scene.ServerNoticeMessageExceptionScene;
import cn.dataplatform.open.common.body.AlarmSceneMessageBody;
import cn.dataplatform.open.common.body.DataSourceMessageBody;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.event.AlarmSceneEvent;
import cn.dataplatform.open.flow.config.RabbitConfig;
import cn.dataplatform.open.flow.service.DataSourceService;
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
public class DataSourceMessageListener {

    @Resource
    private DataSourceService dataSourceService;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue,
            exchange = @Exchange(value = RabbitConfig.SOURCE_EXCHANGE, type = ExchangeTypes.FANOUT)
    ))
    public void onMessage(Message<DataSourceMessageBody> messaging) {
        String requestId = messaging.getHeaders().get(Constant.REQUEST_ID, String.class);
        MDC.put(Constant.REQUEST_ID, requestId);
        DataSourceMessageBody dataSourceMessageBody = messaging.getPayload();
        try {
            log.info("数据源消息:{}", dataSourceMessageBody);
            DataSourceMessageBody.Type type = dataSourceMessageBody.getType();
            switch (type) {
                case UPDATE:
                    this.dataSourceService.remove(dataSourceMessageBody.getId());
                    this.dataSourceService.load(dataSourceMessageBody.getId());
                    break;
                case LOAD:
                    this.dataSourceService.load(dataSourceMessageBody.getId());
                    break;
                case REMOVE:
                    this.dataSourceService.remove(dataSourceMessageBody.getId());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
            ServerNoticeMessageExceptionScene scene = new ServerNoticeMessageExceptionScene(e);
            scene.setQueue(RabbitConfig.SOURCE_QUEUE);
            scene.setExchange(RabbitConfig.SOURCE_EXCHANGE);
            scene.setConsumerClassName(this.getClass().getName());
            scene.setConsumerMethodName(methodName);
            AlarmSceneMessageBody alarmSceneMessageBody = new AlarmSceneMessageBody(scene);
            alarmSceneMessageBody.setWorkspaceCode(dataSourceMessageBody.getWorkspaceCode());
            this.applicationEventPublisher.publishEvent(new AlarmSceneEvent(alarmSceneMessageBody));
            throw e;
        } finally {
            MDC.clear();
        }
    }

}
