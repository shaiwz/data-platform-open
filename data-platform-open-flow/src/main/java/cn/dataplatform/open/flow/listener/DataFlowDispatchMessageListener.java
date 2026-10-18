package cn.dataplatform.open.flow.listener;

import cn.dataplatform.open.common.body.DataFlowDispatchMessageBody;
import cn.dataplatform.open.common.body.DataFlowMessageBody;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.event.DataFlowEvent;
import cn.dataplatform.open.common.server.ServerManager;
import cn.dataplatform.open.common.vo.flow.FlowError;
import cn.dataplatform.open.flow.config.RabbitConfig;
import cn.dataplatform.open.flow.service.core.Flow;
import cn.dataplatform.open.flow.service.core.FlowEngine;
import cn.dataplatform.open.flow.service.core.monitor.FlowMonitor;
import cn.hutool.core.exceptions.ExceptionUtil;
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
import java.util.Objects;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/25
 * @since 1.0.0
 */
@Slf4j
@Component
public class DataFlowDispatchMessageListener {

    @Resource
    private ServerManager serverManager;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private FlowEngine flowEngine;
    @Resource
    private FlowMonitor flowMonitor;

    /**
     * 监听数据流调度消息
     * <p>
     * 消息格式: DataFlowDispatchMessageBody
     * <p>
     * 消息体包含 workspaceCode, flowCode, instanceIds 等信息
     *
     * @param messaging 消息体
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue,
            exchange = @Exchange(value = RabbitConfig.FLOW_DISPATCH_EXCHANGE, type = ExchangeTypes.FANOUT)
    ))
    public void onMessage(Message<DataFlowDispatchMessageBody> messaging) {
        String requestId = messaging.getHeaders().get(Constant.REQUEST_ID, String.class);
        MDC.put(Constant.REQUEST_ID, requestId);
        try {
            DataFlowDispatchMessageBody payload = messaging.getPayload();
            this.onMessage(payload);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 处理数据流调度消息
     *
     * @param payload 数据流调度消息体
     */
    private void onMessage(DataFlowDispatchMessageBody payload) {
        // 获取当前实例ID
        String instanceId = this.serverManager.instanceId();
        log.info("数据流调度消息:{}，当前实例:{}", payload, instanceId);
        List<String> instanceIds = payload.getInstanceIds();
        if (!instanceIds.contains(instanceId)) {
            // 没有调度当前实例，不需要运行
            log.info("数据流调度消息:当前实例不在调度列表中:{}-{}-{}", payload.getWorkspaceCode(), payload.getFlowCode(), instanceId);
            return;
        }
        DataFlowDispatchMessageBody.Type type = payload.getType();
        String workspaceCode = payload.getWorkspaceCode();
        String flowCode = payload.getFlowCode();
        Flow flow = this.flowEngine.get(workspaceCode, flowCode);
        if (flow == null) {
            log.info("数据流调度消息:数据流不存在:{}-{}-{}", workspaceCode, flowCode, instanceId);
            return;
        }
        MDC.put(Constant.FLOW_CODE, flow.getCode());
        // 如果是关闭数据流
        if (Objects.equals(type, DataFlowDispatchMessageBody.Type.STOP)) {
            this.flowEngine.remove(workspaceCode, flowCode);
            return;
        }
        try {
            // 启动数据流
            this.flowEngine.start(workspaceCode, flowCode);
        } catch (Exception e) {
            log.error("数据流调度失败", e);
            // 移除、停止数据流，防止其他实例还在运行，必须一致停止
            DataFlowMessageBody dataFlowMessageBody = new DataFlowMessageBody();
            dataFlowMessageBody.setType(DataFlowMessageBody.Type.REMOVE);
            dataFlowMessageBody.setId(flow.getId());
            dataFlowMessageBody.setWorkspaceCode(workspaceCode);
            this.applicationEventPublisher.publishEvent(new DataFlowEvent(dataFlowMessageBody));
            // 标记启动失败
            Throwable rootCause = ExceptionUtil.getRootCause(e);
            this.flowMonitor.errorWithAlarm(workspaceCode, flowCode, rootCause, FlowError.ErrorType.STARTUP);
        }
    }

}
