package cn.dataplatform.open.flow.service.core.monitor;

import cn.dataplatform.open.common.alarm.scene.DataFlowExecuteExceptionScene;
import cn.dataplatform.open.common.alarm.scene.DataFlowStartFailNoticeScene;
import cn.dataplatform.open.common.enums.RedisKey;
import cn.dataplatform.open.common.event.AlarmSceneEvent;
import cn.dataplatform.open.common.server.ServerManager;
import cn.dataplatform.open.common.vo.flow.FlowError;
import cn.dataplatform.open.flow.service.core.Flow;
import cn.dataplatform.open.flow.service.core.FlowEngine;
import cn.dataplatform.open.flow.service.core.component.FlowComponent;
import cn.hutool.core.exceptions.ExceptionUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/20
 * @since 1.0.0
 */
@Slf4j
@Component
public class FlowMonitor {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ServerManager serverManager;
    @Resource
    private FlowEngine flowEngine;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 标记数据流运行异常
     *
     * @param workspaceCode 工作空间
     * @param flowCode      数据流
     * @param e             异常
     * @param errorType     异常类型
     */
    public void error(String workspaceCode, String flowCode, Throwable e, FlowError.ErrorType errorType) {
        String stacktraceToString = ExceptionUtil.stacktraceToString(e, 1200);
        // 标记数据流运行异常
        try {
            RList<FlowError> flowErrors = this.redissonClient.getList(RedisKey.FLOW_ERROR.build(
                    workspaceCode + "-" + flowCode)
            );
            FlowError flowError = new FlowError();
            flowError.setType(errorType);
            flowError.setTime(LocalDateTime.now());
            flowError.setInstanceId(this.serverManager.instanceId());
            flowError.setMessage(stacktraceToString);
            flowErrors.add(flowError);
            // 保持列表最多5个元素
            if (flowErrors.size() > 5) {
                // 保留最后5个
                flowErrors.trim(flowErrors.size() - 5, flowErrors.size() - 1);
            }
        } catch (Exception ex) {
            log.error("标记数据流异常信息失败,工作空间:{},数据流:{}", workspaceCode, flowCode, ex);
        }
    }


    /**
     * 标记数据流运行异常，并告警
     *
     * @param workspaceCode 工作空间
     * @param flowCode      数据流
     * @param e             异常
     * @param errorType     异常类型
     */
    public void errorWithAlarm(String workspaceCode, String flowCode, Throwable e, FlowError.ErrorType errorType) {
        this.error(workspaceCode, flowCode, e, errorType);
        Flow flow = this.flowEngine.get(workspaceCode, flowCode);
        if (flow == null) {
            return;
        }
        if (!flow.isEnableAlarm()) {
            return;
        }
        // 告警
        DataFlowStartFailNoticeScene noticeScene = new DataFlowStartFailNoticeScene(e);
        noticeScene.setFlowName(flow.getName());
        noticeScene.setFlowCode(flow.getCode());
        this.applicationEventPublisher.publishEvent(new AlarmSceneEvent(flow.getWorkspaceCode(), noticeScene));
    }

    /**
     * 标记数据流运行异常,并告警
     *
     * @param flowComponent 流程组件
     * @param e             异常
     */
    public void errorWithAlarm(FlowComponent flowComponent, Throwable e, FlowError.ErrorType errorType) {
        String workspaceCode = flowComponent.getWorkspaceCode();
        String flowCode = flowComponent.getFlowCode();
        this.error(workspaceCode, flowCode, e, errorType);
        Flow flow = this.flowEngine.get(workspaceCode, flowCode);
        if (flow == null) {
            return;
        }
        if (!flow.isEnableAlarm()) {
            return;
        }
        DataFlowExecuteExceptionScene scene = new DataFlowExecuteExceptionScene(e);
        scene.setFlowCode(flow.getCode());
        scene.setFlowName(flow.getName());
        scene.setComponentCode(flowComponent.getCode());
        scene.setComponentName(flowComponent.getName());
        scene.setComponentClassName(this.getClass().getName());
        this.applicationEventPublisher.publishEvent(new AlarmSceneEvent(flow.getWorkspaceCode(), scene));
    }

    /**
     * 清除最近errorType的错误记录,有的时候说明已经恢复正常了
     *
     * @param workspaceCode 工作空间
     * @param flowCode      数据流编码
     * @param errorType     错误类型
     */
    public void clearRecentError(String workspaceCode, String flowCode, FlowError.ErrorType errorType) {
        RList<FlowError> flowErrors = this.redissonClient.getList(RedisKey.FLOW_ERROR.build(
                workspaceCode + "-" + flowCode)
        );
        if (flowErrors.isEmpty()) {
            return;
        }
        // 从后往前循环删除，直到不是errorType时停止
        for (int i = flowErrors.size() - 1; i >= 0; i--) {
            FlowError error = flowErrors.get(i);
            if (error != null && Objects.equals(error.getType(), errorType)) {
                flowErrors.remove(i);
            } else {
                // 遇到不是指定错误类型时停止删除
                break;
            }
        }
    }

}

