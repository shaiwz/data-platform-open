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
package cn.dataplatform.open.flow.service.core;

import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.enums.RedisKey;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.server.ServerManager;
import cn.dataplatform.open.common.util.ParallelStreamUtils;
import cn.dataplatform.open.common.vo.flow.FlowHeartbeat;
import cn.dataplatform.open.flow.service.core.component.FlowComponent;
import cn.dataplatform.open.flow.service.core.component.JobFlowComponent;
import cn.dataplatform.open.flow.service.core.component.event.DebeziumFlowComponent;
import cn.dataplatform.open.flow.service.core.record.EmptyRecord;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.thread.ThreadUtil;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 流程引擎
 * <p>
 * Destroy method on bean with name 'flowEngine' threw an
 * * exception: org.redisson.RedissonShutdownException: Redisson is shutdown 解决 @DependsOn("redisson")
 * <p>
 * redisson、sourceManager 与 flowEngine 之间存在依赖关系,redisson、sourceManager 会在 flowEngine 之后关闭
 *
 * @author dingqianwen
 * @date 2025/1/4
 * @since 1.0.0
 */
@DependsOn({"redisson", "sourceManager",
        // 保存点等使用
        "dataSource",
        "serverManager"})
@Slf4j
@Component
public class FlowEngine implements InitializingBean {

    /**
     * 数据流心跳使用
     */
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    /**
     * 关闭数据流使用，因为有些数据流关闭时间较长，例如使用了监听等
     */
    private final ThreadPoolExecutor flowStopExecutor = ThreadUtil.newExecutor(2, 20, Integer.MAX_VALUE);

    /**
     * 工作空间 -> 流程编码 -> 流程
     */
    private final Map<String, Map<String, Flow>> flows = new ConcurrentHashMap<>();

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ServerManager serverManager;

    /**
     * 添加流程
     *
     * @param flow 流程
     */
    public void add(Flow flow) {
        Map<String, Flow> flowMap = this.flows.computeIfAbsent(flow.getWorkspaceCode(),
                k -> new ConcurrentHashMap<>()
        );
        flow.setAddTime(LocalDateTime.now());
        flowMap.put(flow.getCode(), flow);
    }

    /**
     * 获取流程
     *
     * @param workspace 工作空间
     * @param flowCode  流程编码
     * @return Flow
     */
    public Flow get(String workspace, String flowCode) {
        Map<String, Flow> flowMap = this.flows.get(workspace);
        if (flowMap == null) {
            return null;
        }
        return flowMap.get(flowCode);
    }


    /**
     * 获取所有数据流
     *
     * @return 所有数据流
     */
    public List<Flow> getAll() {
        if (CollUtil.isEmpty(this.flows)) {
            return Collections.emptyList();
        }
        List<Flow> allFlows = new ArrayList<>();
        for (Map<String, Flow> flowMap : this.flows.values()) {
            allFlows.addAll(flowMap.values());
        }
        return allFlows;
    }

    /**
     * 移除流程,如果流程正在运行,会停止
     *
     * @param workspace 工作空间
     * @param flowCode  流程编码
     */
    public void remove(String workspace, String flowCode) {
        Map<String, Flow> flowMap = this.flows.get(workspace);
        if (flowMap == null) {
            return;
        }
        Flow flow = flowMap.remove(flowCode);
        if (flow == null) {
            return;
        }
        if (flow.isRunning()) {
            log.info("准备停止流程,流程编码:{},实例:{}", flow.getCode(), this.serverManager.instanceId());
            String key = String.format("%s-%s", workspace, flowCode);
            RMap<String, FlowHeartbeat> rMap = this.redissonClient.getMap(RedisKey.FLOW_HEARTBEAT.build(key));
            rMap.remove(this.serverManager.instanceId());
            // 例如debezium 汇聚管道刷最后的数据停止时间较长，需要等待
            Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
            Future<?> submit = this.flowStopExecutor.submit(() -> {
                if (copyOfContextMap != null) {
                    MDC.setContextMap(copyOfContextMap);
                }
                try {
                    this.stop(flow);
                } finally {
                    MDC.clear();
                }
            });
            try {
                // 最多等待6分钟
                submit.get(6, TimeUnit.MINUTES);
                log.info("流程:{} 停止成功,实例:{}", flowCode, this.serverManager.instanceId());
            } catch (Exception e) {
                String mxMsg = "停止流程异常,流程:" + flowCode + ",实例:" + this.serverManager.instanceId();
                if (e instanceof RuntimeException r) {
                    log.error(mxMsg, r);
                    throw r;
                }
                throw new RuntimeException(mxMsg, e);
            }
        }
    }

    /**
     * 销毁
     */
    @PreDestroy
    @Order(Integer.MIN_VALUE)
    public void destroy() {
        log.info("关闭流程引擎");
        // 先关闭心跳检测线程
        this.heartbeatExecutor.shutdown();
        // 关闭流程
        Collection<Map<String, Flow>> values = this.flows.values();
        if (!values.isEmpty()) {
            for (Map<String, Flow> value : values) {
                Collection<Flow> collection = value.values();
                ParallelStreamUtils.forEach(collection, flow -> {
                    String workspaceCode = flow.getWorkspaceCode();
                    String flowCode = flow.getCode();
                    String key = String.format("%s-%s", workspaceCode, flowCode);
                    RMap<String, FlowHeartbeat> rMap = this.redissonClient.getMap(RedisKey.FLOW_HEARTBEAT.build(key));
                    rMap.remove(this.serverManager.instanceId());
                    // 停止流程
                    this.stop(flow);
                }, false);
            }
        }
        this.flowStopExecutor.shutdown();
        log.info("关闭流程引擎成功");
    }


    /**
     * 流程引擎初始化
     */
    @Override
    public void afterPropertiesSet() {
        Runnable runnable = () -> {
            MDC.put(Constant.REQUEST_ID, UUID.fastUUID().toString(true));
            try {
                Collection<Map<String, Flow>> values = this.flows.values();
                if (values.isEmpty()) {
                    return;
                }
                for (Map<String, Flow> value : values) {
                    Collection<Flow> collection = value.values();
                    for (Flow flow : collection) {
                        MDC.put(Constant.FLOW_CODE, flow.getCode());
                        try {
                            boolean running = flow.isRunning();
                            if (!running) {
                                continue;
                            }
                            // 更新流程心跳
                            String key = String.format("%s-%s", flow.getWorkspaceCode(), flow.getCode());
                            RMap<String, FlowHeartbeat> rMap = this.redissonClient.getMap(RedisKey.FLOW_HEARTBEAT.build(key));
                            // 查找并更新当前实例的心跳记录
                            FlowHeartbeat flowHeartbeat = rMap.get(this.serverManager.instanceId());
                            // 如果没有找到记录，则添加新的心跳记录
                            if (flowHeartbeat == null) {
                                FlowHeartbeat newHeartbeat = new FlowHeartbeat();
                                newHeartbeat.setInstanceId(this.serverManager.instanceId());
                                newHeartbeat.setFastHeartbeat(LocalDateTime.now());
                                newHeartbeat.setLastHeartbeat(LocalDateTime.now());
                                rMap.put(this.serverManager.instanceId(), newHeartbeat);
                            } else {
                                // 更新心跳
                                flowHeartbeat.setLastHeartbeat(LocalDateTime.now());
                                // 重新添加到集合中
                                rMap.put(this.serverManager.instanceId(), flowHeartbeat);
                            }
                            if (flow.isDebug()) {
                                log.debug("流程:{} 心跳,实例:{}", key, this.serverManager.instanceId());
                            }
                        } catch (Exception e) {
                            // 防止某个数据流出现问题，导致此次整个心跳异常
                            log.warn("数据流心跳异常,数据流编码:{}-{}", flow.getWorkspaceCode(), flow.getCode(), e);
                        } finally {
                            MDC.remove(Constant.FLOW_CODE);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("数据流心跳异常", e);
            } finally {
                MDC.clear();
            }
        };
        // 定时发送心跳，检查各个实例是否正常运行
        this.heartbeatExecutor.scheduleAtFixedRate(runnable, 10,
                // 10秒检查一次
                10, TimeUnit.SECONDS);
    }

    /**
     * 启动流程
     *
     * @param workspaceCode 工作空间编码
     * @param code          流程编码
     */
    public void start(String workspaceCode, String code) {
        Flow flow = this.get(workspaceCode, code);
        if (flow == null) {
            log.error("流程:{} 不存在", code);
            return;
        }
        try {
            // 启动
            this.start(flow);
        } catch (Exception e) {
            // 有可能部分组件已经启动，但是部分组件启动失败，这里调用一次stop全部终止掉
            // 例如启动mq监听时如果遇到java.net.SocketTimeoutException: Connect timed out，则会导致流程无法启动
            this.stop(flow);
            throw e;
        }
    }

    /**
     * 启动流程
     *
     * @param flow 流程实例
     */
    public void start(Flow flow) {
        log.info("启动流程:{}", flow.getCode());
        // 如果已经在运行,不再启动
        if (flow.getRunning().getAndSet(true)) {
            log.info("流程:{} 已经在运行中", flow.getCode());
            return;
        }
        if (flow.getFlowComponents().isEmpty()) {
            return;
        }
        Set<String> keys = flow.getFlowComponents().keySet();
        for (String key : keys) {
            FlowComponent flowComponent = flow.getFlowComponents().get(key);
            // 如果是监听RabbitMQ消息
            // 如果是监听Kafka消息
            // 如果是定时任务
            Context context = new Context();
            context.setId(UUID.fastUUID().toString(true));
            context.setStartTime(LocalDateTime.now());
            if (flowComponent instanceof JobFlowComponent jobFlowComponent) {
                if (!Objects.equals(jobFlowComponent.getStatus(), Status.ENABLE)) {
                    log.info("定时任务:{} 未启用", jobFlowComponent.getCode());
                    continue;
                }
                // 定时任务,每次执行用一个上下文
                jobFlowComponent.run(null, context);
            } else if (flowComponent instanceof DebeziumFlowComponent debeziumFlowComponent) {
                if (!Objects.equals(debeziumFlowComponent.getStatus(), Status.ENABLE)) {
                    log.info("监听组件:{} 未启用", debeziumFlowComponent.getCode());
                    continue;
                }
                // 流式查询,全局用一个上下文
                debeziumFlowComponent.run(null, context);
            }
        }
        log.info("启动流程完成:{}", flow.getCode());
    }

    /**
     * 根据编码执行某一个组件
     *
     * @param workspaceCode 工作空间编码
     * @param flowCode      流程编码
     * @param componentCode 组件编码
     */
    public void start(String workspaceCode, String flowCode, String componentCode) {
        Flow flow = this.get(workspaceCode, flowCode);
        if (flow == null) {
            log.error("流程:{}-{}不存在", workspaceCode, flowCode);
            return;
        }
        // 判断流程是否已经停止
        if (!flow.isRunning()) {
            log.error("启动组件失败,主流程:{}-{}已经停止", workspaceCode, flowCode);
            return;
        }
        FlowComponent flowComponent = flow.getFlowComponents().get(componentCode);
        if (flowComponent == null) {
            log.error("组件:{}-{}-{}不存在", workspaceCode, flowCode, componentCode);
            return;
        }
        Context context = new Context();
        context.setId(Optional.ofNullable(MDC.get(Constant.REQUEST_ID))
                .orElseGet(() -> UUID.fastUUID().toString(true)));
        context.setStartTime(LocalDateTime.now());
        Transmit transmit = new Transmit();
        transmit.setFlowComponent(null);
        transmit.setRecord(EmptyRecord.INSTANCE);
        flowComponent.run(transmit, context);
    }

    /**
     * 根据编码停止某一个组件
     *
     * @param workspaceCode 工作空间编码
     * @param flowCode      流程编码
     * @param componentCode 组件编码
     */
    public void stop(String workspaceCode, String flowCode, String componentCode) {
        Flow flow = this.get(workspaceCode, flowCode);
        if (flow == null) {
            log.error("流程:{}-{} 不存在", workspaceCode, flowCode);
            return;
        }
        // 判断流程是否已经停止
        if (!flow.isRunning()) {
            log.error("停止组件失败,主流程:{}-{}非运行状态", workspaceCode, flowCode);
            return;
        }
        FlowComponent flowComponent = flow.getFlowComponents().get(componentCode);
        if (flowComponent == null) {
            log.error("组件:{}-{}-{} 不存在", workspaceCode, flowCode, componentCode);
            return;
        }
        flowComponent.stop();
    }

    /**
     * 停止流程
     *
     * @param flow 流程实例
     */
    public void stop(Flow flow) {
        log.info("停止流程:{}", flow.getCode());
        // 如果已经停止,则不再停止
        if (!flow.isRunning()) {
            log.info("流程:{} 已经停止", flow.getCode());
            return;
        }
        if (flow.getFlowComponents().isEmpty()) {
            flow.getRunning().set(false);
            return;
        }
        Thread thread = Thread.currentThread();
        Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
        List<FlowComponent> components = flow.getFlowComponents().values().parallelStream()
                .filter(flowComponent -> {
                    if (copyOfContextMap != null) {
                        MDC.setContextMap(copyOfContextMap);
                    }
                    try {
                        // 先关闭DebeziumFlowComponent,JobFlowComponent
                        boolean preStop = false;
                        if (flowComponent instanceof DebeziumFlowComponent) {
                            preStop = true;
                        } else if (flowComponent instanceof JobFlowComponent) {
                            preStop = true;
                        }
                        if (preStop) {
                            flowComponent.stop();
                            return false;
                        } else {
                            return true;
                        }
                    } catch (Exception e) {
                        log.error("关闭流程组件:{} 失败", flowComponent.getKey(), e);
                        return true;
                    } finally {
                        Thread currentThread = Thread.currentThread();
                        if (thread.threadId() != currentThread.threadId()) {
                            // 如果当前线程不是主线程，则清除MDC
                            // 如果有两个元素，使用parallelStream时，一个使用主线程，一个使用ForkJoinPool
                            MDC.clear();
                        }
                    }
                }).collect(Collectors.toList());
        // 关闭剩余的
        ParallelStreamUtils.forEach(components, (f) -> {
            try {
                f.stop();
            } catch (Exception e) {
                log.error("关闭流程组件:{} 失败", f.getKey(), e);
            }
        }, false);
        log.info("停止流程完成:{}", flow.getCode());
        flow.getRunning().set(false);
    }


}
