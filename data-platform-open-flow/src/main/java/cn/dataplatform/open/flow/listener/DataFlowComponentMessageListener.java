package cn.dataplatform.open.flow.listener;

import cn.dataplatform.open.common.alarm.scene.ServerMessageExceptionScene;
import cn.dataplatform.open.common.body.DataFlowComponentMessageBody;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.event.AlarmSceneEvent;
import cn.dataplatform.open.common.server.ServerManager;
import cn.dataplatform.open.flow.config.RabbitConfig;
import cn.dataplatform.open.flow.service.core.Flow;
import cn.dataplatform.open.flow.service.core.FlowEngine;
import cn.hutool.core.collection.CollUtil;
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

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/14
 * @since 1.0.0
 */
@Slf4j
@Component
public class DataFlowComponentMessageListener {

    @Resource
    private FlowEngine flowEngine;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private ServerManager serverManager;


    /**
     * 数据流组件消息监听
     *
     * @param messaging 数据流组件消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue,
            exchange = @Exchange(value = RabbitConfig.FLOW_COMPONENT_EXCHANGE, type = ExchangeTypes.FANOUT)
    ))
    public void onMessage(Message<DataFlowComponentMessageBody> messaging) {
        String requestId = messaging.getHeaders().get(Constant.REQUEST_ID, String.class);
        MDC.put(Constant.REQUEST_ID, requestId);
        DataFlowComponentMessageBody dataFlowComponentMessageBody = messaging.getPayload();
        String flowCode = dataFlowComponentMessageBody.getFlowCode();
        MDC.put(Constant.FLOW_CODE, flowCode);
        String workspaceCode = dataFlowComponentMessageBody.getWorkspaceCode();
        try {
            log.info("数据流组件消息:{}", dataFlowComponentMessageBody);
            List<String> instanceIds = dataFlowComponentMessageBody.getInstanceIds();
            if (CollUtil.isNotEmpty(instanceIds)) {
                String instanceId = this.serverManager.instanceId();
                // 只有指定的实例才处理
                if (!instanceIds.contains(instanceId)) {
                    log.info("数据流组件消息,工作空间:{}的数据流:{}不包含当前实例:{},不需要处理", workspaceCode, flowCode, instanceId);
                    return;
                }
            }
            Flow flow = this.flowEngine.getFlow(workspaceCode, flowCode);
            if (flow == null) {
                log.info("数据流组件消息,工作空间:{}的数据流:{}不存在,不需要处理", workspaceCode, flowCode);
                return;
            }
            // 如果已经停止,不需要处理
            if (!flow.isRunning()) {
                // 出现这个信息时，也有可能数据流只发了固定某几个实例，没有运行的则会打印此信息
                log.info("数据流组件消息,工作空间:{}的数据流:{}已经停止运行,不需要处理", workspaceCode, flowCode);
                return;
            }
            String componentCode = dataFlowComponentMessageBody.getComponentCode();
            DataFlowComponentMessageBody.Type type = dataFlowComponentMessageBody.getType();
            switch (type) {
                case START:
                    this.flowEngine.start(workspaceCode, flowCode, componentCode);
                    break;
                case STOP:
                    this.flowEngine.stop(workspaceCode, flowCode, componentCode);
                    break;
                case RESTART:
                    this.flowEngine.stop(workspaceCode, flowCode, componentCode);
                    this.flowEngine.start(workspaceCode, flowCode, componentCode);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
            ServerMessageExceptionScene scene = new ServerMessageExceptionScene(e);
            scene.setConsumerClassName(this.getClass().getName());
            scene.setConsumerMethodName(methodName);
            this.applicationEventPublisher.publishEvent(new AlarmSceneEvent(workspaceCode, scene));
            throw e;
        } finally {
            MDC.remove(Constant.REQUEST_ID);
        }
    }

}
