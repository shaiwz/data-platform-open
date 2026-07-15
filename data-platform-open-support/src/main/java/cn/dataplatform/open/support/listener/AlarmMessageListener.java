package cn.dataplatform.open.support.listener;

import cn.dataplatform.open.common.alarm.scene.ServerMessageExceptionScene;
import cn.dataplatform.open.common.body.AlarmMessageBody;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.event.AlarmSceneEvent;
import cn.dataplatform.open.support.config.RabbitConfig;
import cn.dataplatform.open.support.service.alarm.AlarmService;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/22
 * @since 1.0.0
 */
@Slf4j
@Component
public class AlarmMessageListener {

    @Resource
    private AlarmService alarmService;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;


    /**
     * 监听告警消息
     *
     * @param messaging 告警消息
     */
    @RabbitListener(queues = {RabbitConfig.ALARM_QUEUE})
    public void onMessage(Message<AlarmMessageBody> messaging) {
        String requestId = messaging.getHeaders().get(Constant.REQUEST_ID, String.class);
        MDC.put(Constant.REQUEST_ID, requestId);
        AlarmMessageBody alarmMessageBody = messaging.getPayload();
        log.info("告警消息:{}", JSON.toJSONString(alarmMessageBody));
        try {
            this.alarmService.alarm(alarmMessageBody);
        } catch (Exception e) {
            String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
            ServerMessageExceptionScene scene = new ServerMessageExceptionScene(e);
            scene.setConsumerClassName(this.getClass().getName());
            scene.setConsumerMethodName(methodName);
            this.applicationEventPublisher.publishEvent(new AlarmSceneEvent(alarmMessageBody.getWorkspaceCode(), scene));
            throw e;
        } finally {
            MDC.clear();
        }
    }

}
