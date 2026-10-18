package cn.dataplatform.open.flow.service.core.monitor;

import cn.dataplatform.open.common.enums.Micrometer;
import cn.dataplatform.open.flow.service.core.Flow;
import cn.dataplatform.open.flow.service.core.FlowEngine;
import cn.dataplatform.open.flow.service.core.component.FlowComponent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/25
 * @since 1.0.0
 */
@Component
public class FlowComponentMonitor {

    /**
     * 记录run方法调用处理数据数量
     */
    private static final Map<String, Counter> PROCESS_NUMBER_MAP = new ConcurrentHashMap<>();
    /**
     * 记录run方法调用处理数据耗时
     */
    private static final Map<String, Timer> RUN_TIMER_MAP = new ConcurrentHashMap<>();
    /**
     * 记录run方法异常时的次数
     */
    private static final Map<String, Counter> RUN_ERROR_MAP = new ConcurrentHashMap<>();

    @Resource
    private MeterRegistry meterRegistry;
    @Resource
    private FlowEngine flowEngine;

    /**
     * 记录run方法调用处理数据数量
     *
     * @param flowComponent 组件
     * @param number        处理数据数量
     */
    public void processNumber(FlowComponent parentFlowComponent, FlowComponent flowComponent, long number) {
        if (!this.needToMonitor(flowComponent)) {
            return;
        }
        if (number == 0) {
            return;
        }
        String code = flowComponent.getCode();
        String workspaceCode = flowComponent.getWorkspaceCode();
        String flowCode = flowComponent.getFlowCode();
        String flowComponentKey = flowComponent.getKey();
        // 记录run方法调用处理数据数量
        Counter processNumberCounter = PROCESS_NUMBER_MAP.computeIfAbsent(flowComponentKey,
                key ->
                        Counter.builder(Micrometer.FLOW_RUN_PROCESS_NUMBER.getName())
                                .description(Micrometer.FLOW_RUN_PROCESS_NUMBER.getDescription())
                                .tags(Micrometer.WORKSPACE_CODE, workspaceCode, Micrometer.FLOW_CODE, flowCode,
                                        Micrometer.FLOW_COMPONENT_CODE, code,
                                        Micrometer.PARENT_FLOW_COMPONENT_CODE, parentFlowComponent.getCode()
                                )
                                .register(meterRegistry)
        );
        processNumberCounter.increment(number);
    }

    /**
     * 记录run方法调用处理数据耗时
     *
     * @param flowComponent 组件
     * @return 计时器
     */
    public Timer runTimer(FlowComponent parentFlowComponent, FlowComponent flowComponent) {
        if (!this.needToMonitor(flowComponent)) {
            return null;
        }
        String code = flowComponent.getCode();
        String workspaceCode = flowComponent.getWorkspaceCode();
        String flowCode = flowComponent.getFlowCode();
        String flowComponentKey = flowComponent.getKey();
        // 记录run方法调用处理数据耗时
        return RUN_TIMER_MAP.computeIfAbsent(flowComponentKey,
                key ->
                        Timer.builder(Micrometer.FLOW_RUN_TIME.getName())
                                .description(Micrometer.FLOW_RUN_TIME.getDescription())
                                .tags(Micrometer.WORKSPACE_CODE, workspaceCode, Micrometer.FLOW_CODE, flowCode,
                                        Micrometer.FLOW_COMPONENT_CODE, code,
                                        Micrometer.PARENT_FLOW_COMPONENT_CODE, parentFlowComponent.getCode()
                                )
                                .register(meterRegistry)
        );
    }

    /**
     * 记录run方法异常时的次数
     *
     * @param flowComponent 组件
     */
    public void runError(FlowComponent parentFlowComponent, FlowComponent flowComponent) {
        if (this.needToMonitor(flowComponent)) {
            String code = flowComponent.getCode();
            String workspaceCode = flowComponent.getWorkspaceCode();
            String flowCode = flowComponent.getFlowCode();
            String flowComponentKey = flowComponent.getKey();
            // 记录异常次数
            Counter runErrorCounter = RUN_ERROR_MAP.computeIfAbsent(flowComponentKey,
                    key ->
                            Counter.builder(Micrometer.FLOW_RUN_ERROR.getName())
                                    .description(Micrometer.FLOW_RUN_ERROR.getDescription())
                                    .tags(Micrometer.WORKSPACE_CODE, workspaceCode, Micrometer.FLOW_CODE, flowCode,
                                            Micrometer.FLOW_COMPONENT_CODE, code,
                                            Micrometer.PARENT_FLOW_COMPONENT_CODE, parentFlowComponent.getCode()
                                    )
                                    .register(meterRegistry)
            );
            runErrorCounter.increment();
        }
    }

    /**
     * 是否需要监控
     *
     * @param flowComponent 组件
     * @return 是否需要监控 true:需要
     */
    private boolean needToMonitor(FlowComponent flowComponent) {
        Flow flow = this.flowEngine.get(flowComponent.getWorkspaceCode(), flowComponent.getFlowCode());
        if (flow == null) {
            return false;
        }
        return flow.isEnableMonitor();
    }

}
