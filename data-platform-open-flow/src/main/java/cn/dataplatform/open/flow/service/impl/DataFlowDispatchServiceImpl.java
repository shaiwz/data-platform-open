package cn.dataplatform.open.flow.service.impl;

import cn.dataplatform.open.common.body.DataFlowComponentMessageBody;
import cn.dataplatform.open.common.body.DataFlowDispatchMessageBody;
import cn.dataplatform.open.common.body.DataFlowDispatchMessageBody.Type;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.enums.RedisKey;
import cn.dataplatform.open.common.enums.ServerName;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.enums.flow.DataFlowRunStrategy;
import cn.dataplatform.open.common.event.DataFlowComponentEvent;
import cn.dataplatform.open.common.event.DataFlowDispatchEvent;
import cn.dataplatform.open.common.server.Server;
import cn.dataplatform.open.common.server.ServerManager;
import cn.dataplatform.open.common.vo.flow.FlowError;
import cn.dataplatform.open.common.vo.flow.FlowHeartbeat;
import cn.dataplatform.open.flow.service.DataFlowDispatchService;
import cn.dataplatform.open.flow.service.core.Flow;
import cn.dataplatform.open.flow.service.core.FlowEngine;
import cn.dataplatform.open.flow.service.core.component.FlowComponent;
import cn.dataplatform.open.flow.service.core.component.event.DebeziumFlowComponent;
import cn.dataplatform.open.flow.service.core.monitor.FlowMonitor;
import cn.dataplatform.open.flow.vo.data.flow.FlowComponentOnly;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpApplicationContextClosedException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/25
 * @since 1.0.0
 */
@DependsOn("redisson")
@Order(20) // 待数据流加载完后开始启动调度线程
@Slf4j
@Service
public class DataFlowDispatchServiceImpl implements DataFlowDispatchService, ApplicationListener<ServletWebServerInitializedEvent>,
        DisposableBean {

    /**
     * 当前服务器启动后首次调度标识，如果整体集群重启过程中，
     * 导致节点数丢失引发调度，启动后还需要等待3分钟的问题
     */
    private volatile boolean firstDispatch = true;

    private Thread leaderThread;

    /**
     * 数据路调度间隔配置,单位毫秒
     */
    private int dispatchInterval;

    /**
     * 如果cpu内存使用率超过n%，则不调度数据流，阈值配置
     * <p>
     * 默认超过90%不再调度
     */
    @Value("${dp.flow.dispatch.resource-threshold:90}")
    private int resourceThreshold;

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ServerManager serverManager;
    @Resource
    private FlowEngine flowEngine;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private FlowMonitor flowMonitor;


    /**
     * 数据路调度间隔配置,单位毫秒
     *
     * @param dispatchInterval 调度间隔
     */
    @Value("${dp.flow.dispatch.interval:10000}")
    public void setDispatchInterval(int dispatchInterval) {
        // 最小值为5秒，否则抛出异常
        if (dispatchInterval < 5_000) {
            throw new IllegalArgumentException("调度间隔不能小于5秒");
        }
        this.dispatchInterval = dispatchInterval;
    }

    /**
     * 启动调度线程
     *
     * @param event 事件
     */
    @Override
    public void onApplicationEvent(@NonNull ServletWebServerInitializedEvent event) {
        Runnable runnable = () -> {
            // 等待*秒，确保服务内部组件以及其他服务全部加载完毕
            ThreadUtil.sleep(30, TimeUnit.SECONDS);
            // destroy 方法会将leaderThread设置为null，表示停止调度线程
            while (this.leaderThread != null) {
                RLock lock = null;
                try {
                    lock = this.redissonClient.getLock(RedisKey.FLOW_DISPATCH_LEADER_LOCK.getKey());
                    // 尝试获取锁
                    if (lock.tryLock()) {
                        log.info("获取调度权,当前实例:{}", this.serverManager.instanceId());
                        // 作为leader，需要定期续期锁
                        while (!Thread.currentThread().isInterrupted()
                                // a实例拿到锁，进入循环开始执行，其他3个实例每隔ns拿锁判断是否有运行中的调度master
                                // 但是a实例因为redis故障导致无法正常续期，但是里面的循环还在执行，然后其他实例拿到锁开始执行
                                // 这里每次调度钱都会检测当前线程是否还持有锁，否则让出调度权
                                && lock.isLocked()
                                && lock.isHeldByCurrentThread()) {
                            // 每次使用新的请求ID，避免日志冲突
                            MDC.put(Constant.REQUEST_ID, UUID.randomUUID().toString(true));
                            // 每隔n秒检测调度一次数据流
                            this.dispatch();
                            // 首次调度标识作废
                            this.firstDispatch = false;
                            // 休眠一段时间，等待下次调度
                            ThreadUtil.sleep(this.dispatchInterval);
                        }
                        // 跳出while 循环，有可能当前服务锁续期失败，当redis服务连接出现问题时
                        // 当前线程休眠
                        log.info("当前实例:{}让出调度权,等待下次获取调度权", this.serverManager.instanceId());
                        ThreadUtil.sleep(5000);
                    } else {
                        // 其他节点获取到锁进行调度，当前节点首次标识也设置为false
                        this.firstDispatch = false;
                        // 不是Leader，等待并重试，获取调度权
                        log.debug("当前实例:{}未获取到调度权,尝试下次获取", this.serverManager.instanceId());
                        ThreadUtil.sleep(5000);
                    }
                } catch (AmqpApplicationContextClosedException e) {
                    // 当服务在重启中进行调度时，此时rabbitmq连接已经关闭，捕获该异常，直接跳过
                    log.warn("调度选举中断,当前实例:{}服务上下文已关闭", this.serverManager.instanceId(), e);
                } catch (Exception e) {
                    log.error("调度选举失败,当前实例:{},错误:{}", this.serverManager.instanceId(), e.getMessage(), e);
                } finally {
                    if (lock != null) {
                        try {
                            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                                lock.unlock();
                                log.info("释放调度权:" + this.serverManager.instanceId());
                            }
                        } catch (Exception e) {
                            log.error("释放调度权失败,当前实例:{}", this.serverManager.instanceId(), e);
                        }
                    }
                    MDC.clear();
                }
            }
        };
        this.leaderThread = new Thread(runnable);
        this.leaderThread.start();
    }

    /**
     * 调度数据流
     */
    @Override
    public void dispatch() {
        log.info("调度数据流,当前实例:{}", this.serverManager.instanceId());
        List<Flow> allFlows = this.flowEngine.getAll();
        if (allFlows.isEmpty()) {
            log.info("没有数据流需要调度");
            return;
        }
        // 获取可用的服务实例
        Collection<Server> servers = this.serverManager.availableList(ServerName.FLOW_SERVER.getValue());
        if (CollUtil.isEmpty(servers)) {
            // 当redis故障时，servers为空，则跳过本次调度
            log.error("没有可用的服务实例,无法调度数据流");
            return;
        }
        log.info("所有在线服务实例列表:" + JSON.toJSONString(servers));
        // 获取可用的服务实例ID
        Set<String> availableInstanceIds = servers.stream()
                .map(Server::getInstanceId).collect(Collectors.toSet());
        for (Flow flow : allFlows) {
            try {
                MDC.put(Constant.FLOW_CODE, flow.getCode());
                String flowCode = flow.getCode();
                String workspaceCode = flow.getWorkspaceCode();
                String key = String.format("%s-%s", workspaceCode, flowCode);
                List<String> dispatchStartInstanceIds = new ArrayList<>();
                List<String> dispatchStopInstanceIds = new ArrayList<>();
                this.planInstanceDispatch(flow, availableInstanceIds, servers, dispatchStartInstanceIds, dispatchStopInstanceIds);
                // 最近是否调度过
                if (CollUtil.isNotEmpty(dispatchStartInstanceIds) || CollUtil.isNotEmpty(dispatchStopInstanceIds)) {
                    // 集群整体首次启动时，先忽略启动前的调度状态
                    if (Objects.equals(this.firstDispatch, false)) {
                        // 如果数据流最近3分钟已经调度过，则不用重新调度，等待启动中，防止重复调度
                        RLock dispatchLock = this.redissonClient.getLock(RedisKey.FLOW_DISPATCH_LOCK.build(key));
                        if (!dispatchLock.tryLock(0, 3, TimeUnit.MINUTES)) {
                            log.info("数据流 {}-{} 最近3分钟内已经调度过,等待执行完成,跳过本次调度", workspaceCode, flowCode);
                            continue;
                        }
                    }
                } else {
                    // 最近没有需要调度的数据流，则去判断是否有组件还在正常运行
                    // 否则本次应该等待数据流先调度，完成运行
                    this.detectionComponentOnly(flow);
                    continue;
                }
                // 调度需要启动的数据流实例
                if (CollUtil.isNotEmpty(dispatchStartInstanceIds)) {
                    // cpu内存超过阈值的服务实例不调度,均值resourceThreshold%以上时
                    // 防止某节点占用率过高，再去增加任务，导致节点崩溃
                    Stream<Server> dispatchStartInstance = servers.stream()
                            .filter(s -> dispatchStartInstanceIds.contains(s.getInstanceId()));
                    List<Server> dispatchServers = dispatchStartInstance.filter(f -> {
                        // 例如：22.17
                        BigDecimal cpuUsageRatio = f.getCpuUsageRatio();
                        // 例如：60.1
                        BigDecimal memoryUsageRatio = f.getMemoryUsageRatio();
                        return cpuUsageRatio.compareTo(new BigDecimal(this.resourceThreshold)) < 0 &&
                                memoryUsageRatio.compareTo(new BigDecimal(this.resourceThreshold)) < 0;
                    }).toList();
                    if (CollUtil.isEmpty(dispatchServers)) {
                        // 数据流调度实例时,被调度实例CPU内存整体占用率过高,等待下次调度
                        log.warn("数据流 {}-{} 调度失败，所有候选实例CPU/内存使用率超过{}%阈值,等待下次调度", workspaceCode, flowCode, this.resourceThreshold);
                        this.flowMonitor.errorWithAlarm(flow.getWorkspaceCode(), flow.getCode(),
                                new Exception("所有候选实例CPU/内存使用率过高(>=" + this.resourceThreshold + "%),等待下次调度"),
                                FlowError.ErrorType.WARNING_103);
                        // 如果没有可用的调度节点，则跳过本次调度
                        continue;
                    } else {
                        // 恢复时，清除所有候选实例CPU/内存使用率过高的告警
                        this.flowMonitor.clearRecentError(flow.getWorkspaceCode(), flow.getCode(), FlowError.ErrorType.WARNING_103);
                    }
                    // 存在调度节点，但是缺少,正常调度，但是发起一个警告，表示存在调度节点内存不足
                    if (dispatchStartInstanceIds.size() > dispatchServers.size()) {
                        // 计算被过滤掉的实例数量
                        int filteredCount = dispatchStartInstanceIds.size() - dispatchServers.size();
                        List<String> highUsageInstances = dispatchStartInstanceIds.stream()
                                .filter(id -> !dispatchServers.stream()
                                        .map(Server::getInstanceId)
                                        .collect(Collectors.toSet()).contains(id))
                                .collect(Collectors.toList());
                        log.warn("数据流 {}-{} 有 {} 个实例因CPU/内存使用率过高被过滤: {}", workspaceCode, flowCode, filteredCount, highUsageInstances);
                        this.flowMonitor.errorWithAlarm(flow.getWorkspaceCode(), flow.getCode(),
                                new Exception(String.format("%d个实例CPU/内存使用率过高(>=%d%%),无法调度: %s", filteredCount, this.resourceThreshold, highUsageInstances)),
                                FlowError.ErrorType.WARNING_104);
                    } else {
                        // 恢复时，清除部分实例CPU/内存使用率过高的告警
                        this.flowMonitor.clearRecentError(flow.getWorkspaceCode(), flow.getCode(), FlowError.ErrorType.WARNING_104);
                    }
                    // 需要调度
                    DataFlowDispatchMessageBody dispatchMessageBody = new DataFlowDispatchMessageBody();
                    dispatchMessageBody.setType(Type.START);
                    dispatchMessageBody.setFlowCode(flowCode);
                    dispatchMessageBody.setWorkspaceCode(workspaceCode);
                    dispatchMessageBody.setInstanceIds(dispatchStartInstanceIds);
                    log.info("调度数据流,调度启动实例:{}-{}-{}", workspaceCode, flowCode, dispatchStartInstanceIds);
                    this.applicationEventPublisher.publishEvent(new DataFlowDispatchEvent(dispatchMessageBody));
                }
                // 调度需要停止的数据流实例
                if (CollUtil.isNotEmpty(dispatchStopInstanceIds)) {
                    // 剔除多余的节点
                    DataFlowDispatchMessageBody dispatchMessageBody = new DataFlowDispatchMessageBody();
                    dispatchMessageBody.setType(Type.STOP);
                    dispatchMessageBody.setFlowCode(flowCode);
                    dispatchMessageBody.setWorkspaceCode(workspaceCode);
                    dispatchMessageBody.setInstanceIds(dispatchStopInstanceIds);
                    log.info("调度数据流,调度停止实例:{}-{}-{}", workspaceCode, flowCode, dispatchStopInstanceIds);
                    this.applicationEventPublisher.publishEvent(new DataFlowDispatchEvent(dispatchMessageBody));
                }
            } catch (AmqpApplicationContextClosedException a) {
                // 当服务在重启中进行调度时，此时rabbitmq连接已经关闭，直接中断调度
                throw a;
            } catch (Exception e) {
                log.error("调度数据流失败,当前实例:{}", this.serverManager.instanceId(), e);
                // 标记启动失败
                this.flowMonitor.errorWithAlarm(flow.getWorkspaceCode(), flow.getCode(), e,
                        FlowError.ErrorType.STARTUP);
            } finally {
                MDC.remove(Constant.FLOW_CODE);
            }
        }
    }


    /**
     * 规划数据流实例调度
     *
     * @param flow                     数据流
     * @param availableInstanceIds     可用的服务实例ID
     * @param servers                  可用的服务实例
     * @param dispatchStartInstanceIds 需要调度启动的数据流实例ID
     * @param dispatchStopInstanceIds  需要调度停止的数据流实例ID
     */
    private void planInstanceDispatch(Flow flow, Set<String> availableInstanceIds,
                                      Collection<Server> servers, List<String> dispatchStartInstanceIds,
                                      List<String> dispatchStopInstanceIds) {
        String workspaceCode = flow.getWorkspaceCode();
        String flowCode = flow.getCode();
        String key = String.format("%s-%s", workspaceCode, flowCode);
        RMap<String, FlowHeartbeat> flowHeartbeatMap = this.redissonClient.getMap(RedisKey.FLOW_HEARTBEAT.build(key));
        // 获取健康的数据流实例
        List<FlowHeartbeat> normalFlows = flowHeartbeatMap.values().stream().filter(FlowHeartbeat::isNormal).toList();
        List<String> healthyFlowInstanceIds = normalFlows.stream().map(FlowHeartbeat::getInstanceId).collect(Collectors.toList());
        log.info("当前数据流:{}-{}运行中的实例:{}", workspaceCode, flowCode, healthyFlowInstanceIds);
        DataFlowRunStrategy runStrategy = flow.getRunStrategy();
        switch (runStrategy) {
            case ALL_INSTANCES:
                // 如果运行中的数据流数量小于可用的服务实例数量，则需要调度
                if (healthyFlowInstanceIds.size() < availableInstanceIds.size()) {
                    // 调度没有运行的实例节点
                    for (Server server : servers) {
                        String instanceId = server.getInstanceId();
                        if (!healthyFlowInstanceIds.contains(instanceId)) {
                            // 是否考虑，如果某个节点cpu 内存使用率过高，则不调度 ?
                            // 后续看情况规划
                            dispatchStartInstanceIds.add(instanceId);
                        }
                    }
                }
                break;
            case SPECIFY_INSTANCES: {
                List<String> specifyInstances = flow.getSpecifyInstances();
                // 如果指定的实例没有在运行，且实例存在，则调度
                List<String> not = new ArrayList<>();
                for (String instanceId : specifyInstances) {
                    // 如果指定的实例不存在，则跳过
                    if (!availableInstanceIds.contains(instanceId)) {
                        log.warn("指定调度的实例不存在,实例:{}", instanceId);
                        not.add(instanceId);
                        continue;
                    }
                    // 如果指定的实例没有在运行，则调度
                    if (!healthyFlowInstanceIds.contains(instanceId)) {
                        dispatchStartInstanceIds.add(instanceId);
                    }
                }
                int i = dispatchStartInstanceIds.size() + healthyFlowInstanceIds.size();
                if (i == 0) {
                    // 指定的数据流服务都不在线，即没有在运行的数据流，也没有再可调度的服务
                    this.flowMonitor.errorWithAlarm(flow.getWorkspaceCode(), flow.getCode(),
                            new Exception("数据流运行时指定的数据流实例都不在线"), FlowError.ErrorType.STARTUP);
                } else {
                    if (CollUtil.isNotEmpty(not)) {
                        // 存在部分实例不存在
                        String collected = String.join(",", not);
                        this.flowMonitor.errorWithAlarm(flow.getWorkspaceCode(), flow.getCode(),
                                new Exception("数据流运行时指定的数据流实例不存在:" + collected), FlowError.ErrorType.WARNING_102);
                    } else {
                        // 正常运行，或者恢复正常
                        this.flowMonitor.clearRecentError(flow.getWorkspaceCode(), flow.getCode(), FlowError.ErrorType.WARNING_102);
                    }
                }
                break;
            }
            case FIXED_INSTANCE_NUMBER:
                Integer instanceNumber = flow.getInstanceNumber();
                // 如果运行的实例数量小于指定的实例数量，则需要调度，找到一个内存cpu占用率最低的实例
                if (healthyFlowInstanceIds.size() < instanceNumber) {
                    // servers 按照 cpuUsageRatio memoryUsageRatio使用率排序，综合占用率最低的放前面，待取号
                    List<Server> sortedServers = Lists.newArrayList(servers);
                    sortedServers.sort((o1, o2) -> {
                        // 计算综合占用率
                        BigDecimal o1Usage = o1.getMemoryUsageRatio().add(o1.getCpuUsageRatio());
                        BigDecimal o2Usage = o2.getMemoryUsageRatio().add(o2.getCpuUsageRatio());
                        return o1Usage.compareTo(o2Usage);
                    });
                    // 补充需要调度的数据流数量
                    int needDispatchCount = instanceNumber - healthyFlowInstanceIds.size();
                    log.info("当前实例数量:{},指定实例数量:{},需要调度数量:{}", healthyFlowInstanceIds.size(),
                            instanceNumber, needDispatchCount);
                    for (int i = 0; i < needDispatchCount; i++) {
                        if (i >= sortedServers.size()) {
                            break;
                        }
                        Server server = sortedServers.get(i);
                        String instanceId = server.getInstanceId();
                        if (!healthyFlowInstanceIds.contains(instanceId)) {
                            dispatchStartInstanceIds.add(instanceId);
                        }
                    }
                } else if (healthyFlowInstanceIds.size() > instanceNumber) {
                    // 超出，是否关闭多余的？
                    // 什么情况下会超出呢？除非BUG了，先打个日志
                    log.warn("数据流实例数量超过指定的实例数量,指定实例数量:{},当前实例数量:{},当前实例明细:{}", instanceNumber,
                            healthyFlowInstanceIds.size(), healthyFlowInstanceIds);
                    // 服务器暂时因网络问题宕机了，然后又恢复了，但是期间增加了其他节点的机器，需要剔除后来者
                    // 按照fastHeartbeat排序，拿到一个注册时间最晚的节点
                    List<FlowHeartbeat> flowHeartbeats = normalFlows.stream()
                            .sorted(Comparator.comparing(FlowHeartbeat::getFastHeartbeat).reversed())
                            .toList();
                    // 取出前 instanceNumber 个节点
                    int excludeNodeCount = healthyFlowInstanceIds.size() - instanceNumber;
                    for (FlowHeartbeat flowHeartbeat : flowHeartbeats) {
                        if (dispatchStopInstanceIds.size() >= excludeNodeCount) {
                            break;
                        }
                        String instanceId = flowHeartbeat.getInstanceId();
                        dispatchStopInstanceIds.add(instanceId);
                    }
                    break;
                }
                int i = dispatchStartInstanceIds.size() + healthyFlowInstanceIds.size();
                if (i < instanceNumber) {
                    // 说明缺少节点，例如选择使用2个实例，但是只启动了1个实例
                    log.warn("数据流运行实例数量不足,当前待调度+运行中实例数量:{},指定实例数量:{}", i, instanceNumber);
                    // 警告，但是不影响运行
                    this.flowMonitor.errorWithAlarm(flow.getWorkspaceCode(), flow.getCode(),
                            new Exception("数据流运行实例数量不足，指定实例数量:" + instanceNumber + ",缺少:" + (instanceNumber - i) + "个实例"),
                            FlowError.ErrorType.WARNING_101);
                } else if (i == instanceNumber) {
                    // 恢复时，需要清除数据流实例数量不足的告警
                    this.flowMonitor.clearRecentError(flow.getWorkspaceCode(), flow.getCode(), FlowError.ErrorType.WARNING_101);
                }
                break;
            default:
                throw new UnsupportedOperationException("不支持的调度策略:" + runStrategy.getName());
        }
    }

    /**
     * 监控 debezium 等组件是否正常,如果异常，则重启
     * <p>
     * 当运行数据流的某个节点死了,或者被缩容到，其他节点接力
     *
     * @param flow 数据流
     */
    private void detectionComponentOnly(Flow flow) {
        // 如果组件没有在运行，跳过
        if (!flow.isRunning()) {
            return;
        }
        LocalDateTime addTime = flow.getAddTime();
        if (addTime.isAfter(LocalDateTime.now().minusMinutes(2))) {
            // 启动时间在2分钟内，跳过检测，有可能还没初始化完毕
            return;
        }
        Map<String, FlowComponent> flowComponents = flow.getFlowComponents();
        Collection<FlowComponent> values = flowComponents.values();
        for (FlowComponent flowComponent : values) {
            // 找到是debezium组件的
            if (!(flowComponent instanceof DebeziumFlowComponent component)) {
                continue;
            }
            // 如果组件是禁用状态，跳过
            if (Objects.equals(component.getStatus(), Status.DISABLE)) {
                continue;
            }
            if (flow.isDebug()) {
                log.info("数据流组件:{} 守护监听执行", component.getKey());
            }
            String key = RedisKey.FLOW_COMPONENT_ONLY.build(component.getKey());
            RBucket<FlowComponentOnly> flowComponentOnlyRBucket = this.redissonClient.getBucket(key);
            try {
                FlowComponentOnly flowDebeziumHeartbeat = flowComponentOnlyRBucket.get();
                DataFlowComponentMessageBody.Type type;
                if (flowDebeziumHeartbeat == null) {
                    // 没有启动过，触发启动
                    log.info("数据流组件:{} 守护监听执行,当前实例:{} 未检测到组件运行信息,准备启动", component.getKey(), this.serverManager.instanceId());
                    type = DataFlowComponentMessageBody.Type.START;
                } else {
                    LocalDateTime startTime = flowDebeziumHeartbeat.getStartTime();
                    String instanceId = flowDebeziumHeartbeat.getInstanceId();
                    // 节点状态正常，无需处理，事件心跳是否正常
                    if (this.isThereHeartbeat(component, instanceId)) {
                        // 正常运行
                        continue;
                    } else {
                        // 节点非正常需要移除当前运行节点运行标识，然后触发重新启动
                        flowComponentOnlyRBucket.delete();
                    }
                    log.warn("数据流组件:{} 守护监听执行,当前实例:{} 节点异常,准备重启,上一个运行实例:{} 上次启动时间:{}",
                            component.getKey(), this.serverManager.instanceId(), instanceId, startTime);
                    // 如果最近几分钟有调度重启过当前组件，则等待3分钟后
                    RLock lock = this.redissonClient.getLock(RedisKey.FLOW_COMPONENT_MESSAGE_LOCK.build(flowComponent.getKey()));
                    if (!lock.tryLock(0, 3, TimeUnit.MINUTES)) {
                        log.info("数据流组件 {} 最近3分钟内已经发布过消息,等待执行完成,跳过本次发布", flowComponent.getKey());
                        continue;
                    }
                    type = DataFlowComponentMessageBody.Type.RESTART;
                }
                // 重启当前组件
                log.info("数据流组件:{} 守护监听执行,当前实例:{} 需要:{}", component.getKey(), this.serverManager.instanceId(), type);
                DataFlowComponentMessageBody dataFlowComponentMessageBody = new DataFlowComponentMessageBody();
                dataFlowComponentMessageBody.setWorkspaceCode(component.getWorkspaceCode());
                dataFlowComponentMessageBody.setFlowCode(component.getFlowCode());
                dataFlowComponentMessageBody.setComponentCode(component.getCode());
                // 触发Debezium组件重启
                dataFlowComponentMessageBody.setType(type);
                this.applicationEventPublisher.publishEvent(new DataFlowComponentEvent(dataFlowComponentMessageBody));
            } catch (Exception e) {
                log.error("数据流组件:{} 守护监听执行异常", component.getKey(), e);
            }
        }
    }

    /**
     * 监听组件是否还有心跳
     *
     * @param instanceId 检测的实例ID
     * @return 返回true时表示假死，需要当前节点运行，返回false时表示还在运行
     */
    public boolean isThereHeartbeat(DebeziumFlowComponent component, String instanceId) {
        RBucket<Long> bucket = this.redissonClient.getBucket(
                RedisKey.FLOW_DEBEZIUM_HEARTBEAT.build(component.getKey() + "-" + instanceId));
        // 是否还有心跳
        Long lastHeartbeat = bucket.get();
        if (lastHeartbeat == null) {
            // 没有心跳，表示假死
            log.info("数据流组件:{} 运行实例:{} 没有检测到心跳,组件已死亡", component.getKey(), instanceId);
            return false;
        }
        // 有心跳，表示还在运行
        return true;
    }


    /**
     * 停止调度线程
     */
    @Override
    public void destroy() {
        log.info("停止数据流调度线程,当前实例:{}", this.serverManager.instanceId());
        if (this.leaderThread != null) {
            this.leaderThread.interrupt();
            this.leaderThread = null;
        }
    }

}
