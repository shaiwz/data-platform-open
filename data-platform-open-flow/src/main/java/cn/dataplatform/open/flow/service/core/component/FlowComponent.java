/*
 * ============================================================================
 *
 *                    数海文舟 (DATA PLATFORM) 版权所有 © 2025
 *
 *       本软件受著作权法和国际版权条约保护。
 *       未经明确书面授权，任何单位或个人不得对本软件进行复制、修改、分发、
 *       逆向工程、商业用途等任何形式的非法使用。违者将面临人民币100万元的
 *       法定罚款及可能的法律追责。
 *
 *       举报侵权行为可获得实际罚款金额40%的现金奖励。
 *       法务邮箱：761945125@qq.com
 *
 *       COPYRIGHT (C) 2025 dingqianwen COMPANY. ALL RIGHTS RESERVED.
 *
 * ============================================================================
 */
package cn.dataplatform.open.flow.service.core.component;

import cn.dataplatform.open.common.util.ParallelStreamUtils;
import cn.dataplatform.open.flow.exception.DataFlowNoRunningException;
import cn.dataplatform.open.flow.exception.DataFlowRunException;
import cn.dataplatform.open.flow.service.core.Context;
import cn.dataplatform.open.flow.service.core.Flow;
import cn.dataplatform.open.flow.service.core.Transmit;
import cn.dataplatform.open.flow.service.core.pack.StopWatch;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/4
 * @since 1.0.0
 */
@Slf4j
public abstract class FlowComponent {

    @Getter
    private final Flow flow;
    /**
     * 当前组件的唯一标识
     */
    @Getter
    private final String code;

    @Setter
    @Getter
    private String name;

    /**
     * 下一个节点
     * <p>
     * 外层list为相同优先级的节点，内层为某个优先级的所有节点
     * <p>
     * 按照顺序一个优先级一个优先级的执行
     */
    private final List<List<FlowComponent>> next = new CopyOnWriteArrayList<>();

    /**
     * 构造方法
     *
     * @param flow 流程
     * @param code 当前组件
     */
    public FlowComponent(Flow flow, String code) {
        this.flow = flow;
        this.code = code;
    }

    /**
     * 添加下一个节点
     *
     * @param flowComponent 下一个节点
     */
    public void addNext(FlowComponent flowComponent) {
        Objects.requireNonNull(flowComponent);
        this.next.add(Collections.singletonList(flowComponent));
    }

    /**
     * 添加一组下一个节点（相同优先级的数据）
     *
     * @param flowComponent 下一个节点
     */
    public void addNext(List<FlowComponent> flowComponent) {
        Objects.requireNonNull(flowComponent);
        this.next.add(flowComponent);
    }

    /**
     * 执行
     */
    public abstract void run(Transmit transmit, Context context);

    /**
     * 下一个节点存在多个
     *
     * @return 下一个节点
     */
    public List<List<FlowComponent>> next() {
        return this.next;
    }

    /**
     * 执行下一个节点
     *
     * @param supplier 传输对象
     * @param context  上下文
     */
    public void runNext(Supplier<Transmit> supplier, Context context) {
        if (CollUtil.isEmpty(this.next)) {
            return;
        }
        // 调用runNext
        this.runNext(supplier.get(), context);
    }

    /**
     * 执行下一个节点
     *
     * @param transmit 传输对象
     * @param context  上下文
     */
    public void runNext(Transmit transmit, Context context) {
        if (CollUtil.isEmpty(this.next)) {
            return;
        }
        // 检测是否还在运行
        if (!this.isRunning()) {
            throw new DataFlowNoRunningException("数据流未运行或已关闭, 不再执行后续节点");
        }
        // 线程是否已经被标记中断
        if (Thread.currentThread().isInterrupted()) {
            throw new DataFlowRunException("线程已被中断, 不再执行后续节点");
        }
        StopWatch timer = transmit.getTimer();
        // 先停止计时，后续监控节点不再记录子节点总耗时，而是单独计算当前节点的
        if (timer != null) {
            timer.stop();
        }
        log.info("当前节点: {}({}), 传递数据到下一个节点", this.getName(), this.getCode());
        // 按照优先级顺序执行下一个节点
        for (List<FlowComponent> flowComponents : this.next) {
            // 如果flowComponents只有一个，不用使用并行流了
            if (flowComponents.size() == 1) {
                FlowComponent flowComponentsFirst = flowComponents.getFirst();
                flowComponentsFirst.run(transmit, context);
                continue;
            }
            // 多个使用并行流并行执行
            ParallelStreamUtils.forEach(flowComponents, flowComponent -> {
                // 执行下一个节点
                flowComponent.run(transmit, context);
            });
        }
        // 继续计时
        if (timer != null) {
            timer.start();
        }
    }

    /**
     * 停止
     */
    public void stop() {
    }

    /**
     * 获取Spring上下文
     *
     * @return ApplicationContext
     */
    public ApplicationContext getApplicationContext() {
        return SpringUtil.getApplicationContext();
    }

    /**
     * 获取当前组件的唯一标识
     *
     * @return 唯一标识
     */
    public String getKey() {
        return this.getWorkspaceCode() + "-" + this.getFlowCode() + "-" + this.code;
    }

    /**
     * 当前数据流的工作空间
     *
     * @return 工作空间
     */
    public String getWorkspaceCode() {
        return this.flow.getWorkspaceCode();
    }

    /**
     * 获取数据流编码
     *
     * @return 数据流编码
     */
    public String getFlowCode() {
        return this.flow.getCode();
    }

    /**
     * 是否运行中
     *
     * @return 是否运行中
     */
    public boolean isRunning() {
        return this.flow.isRunning();
    }

    /**
     * 是否调试模式
     *
     * @return 是否调试模式
     */
    public boolean isDebug() {
        return this.flow.isDebug();
    }

}
