package cn.dataplatform.open.flow.service.core.component.event;

import cn.dataplatform.open.common.body.DataFlowComponentMessageBody;
import cn.dataplatform.open.common.body.DataFlowMessageBody;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.enums.RedisKey;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.event.DataFlowComponentEvent;
import cn.dataplatform.open.common.event.DataFlowEvent;
import cn.dataplatform.open.common.event.EventPublisher;
import cn.dataplatform.open.common.server.ServerManager;
import cn.dataplatform.open.common.source.Source;
import cn.dataplatform.open.common.source.SourceManager;
import cn.dataplatform.open.common.vo.flow.FlowError;
import cn.dataplatform.open.flow.service.core.Context;
import cn.dataplatform.open.flow.service.core.Flow;
import cn.dataplatform.open.flow.service.core.Transmit;
import cn.dataplatform.open.flow.service.core.component.FlowComponent;
import cn.dataplatform.open.flow.service.core.component.event.connector.ConnectorConfigure;
import cn.dataplatform.open.flow.service.core.component.event.convert.BinaryConverter;
import cn.dataplatform.open.flow.service.core.component.event.convert.DateTimeConverter;
import cn.dataplatform.open.flow.service.core.monitor.FlowMonitor;
import cn.dataplatform.open.flow.vo.data.flow.FlowComponentOnly;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.extra.spring.SpringUtil;
import io.debezium.config.Configuration;
import io.debezium.data.Envelope;
import io.debezium.embedded.Connect;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.KeyValueHeaderChangeEventFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.source.SourceRecord;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * 〈 <a href="https://debezium.io/documentation/reference/3.0/connectors/mysql.html">debezium documentation</a>〉
 *
 * @author dingqianwen
 * @date 2025/1/7
 * @since 1.0.0
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class DebeziumFlowComponent extends FlowComponent {

    /**
     * DebeziumFlow 专用线程池
     */
    private ExecutorService execute;
    private Future<?> executeFuture;
    /**
     * 心跳保持
     */
    private ScheduledExecutorService heartBeatScheduler;

    private RedissonClient redissonClient;
    private SourceManager sourceManager;
    private ServerManager serverManager;

    @NotBlank
    private String datasourceCode;
    /**
     * 逗号隔开
     */
    @NotBlank
    private String schemas;
    /**
     * 逗号隔开
     * <p>
     * schema.table
     */
    private String tables;
    /**
     * 状态
     */
    private Status status = Status.ENABLE;
    /**
     * 自定义配置信息
     */
    private Properties properties;

    /**
     * 监听的操作类型
     */
    private List<Envelope.Operation> operations = Arrays.asList(Envelope.Operation.READ,
            Envelope.Operation.CREATE,
            Envelope.Operation.UPDATE,
            Envelope.Operation.DELETE);

    private DebeziumEngine<ChangeEvent<SourceRecord, SourceRecord>> debeziumEngine;

    /**
     * 启动策略
     */
    private StartStrategy startStrategy = StartStrategy.AUTO;
    /**
     * 保存间隔(毫秒)
     */
    private Integer savePointInterval = 5000;
    /**
     * 保留时长(天)
     */
    private Integer savePointDuration = 30;
    /**
     * 保存点唯一编码,优先级高于startStrategy
     */
    private String savePoint;

    /**
     * 如果没有指定,则自己随机生成一个,从5400-6400中随机分配一个数字
     */
    private Integer databaseServerId;
    /**
     * oracle listener server name
     * SELECT name FROM v$services;
     * XE or ORCLCDB ...
     */
    private String listenerServerName;

    /**
     * max.batch.size
     */
    private Integer maxBatchSize = 2048;

    /**
     * GMT/UTC
     */
    private String connectionTimeZone = "GMT+08";

    /**
     * 线程数
     */
    private Integer threadNumber;

    private FlowMonitor flowMonitor;

    public DebeziumFlowComponent(Flow flow, String code) {
        super(flow, code);
        this.redissonClient = SpringUtil.getBean(RedissonClient.class);
        this.sourceManager = SpringUtil.getBean(SourceManager.class);
        this.serverManager = SpringUtil.getBean(ServerManager.class);
        this.flowMonitor = this.getApplicationContext().getBean(FlowMonitor.class);
    }

    /**
     * 执行
     *
     * @param transmit 传输
     * @param context  上下文
     */
    @SneakyThrows
    @Override
    public void run(@Nullable Transmit transmit, Context context) {
        if (CollUtil.isEmpty(this.operations)) {
            throw new IllegalArgumentException("监听的操作类型不能为空");
        }
        Source source = this.sourceManager.get(this.getWorkspaceCode(), this.datasourceCode);
        if (source == null) {
            throw new IllegalArgumentException("数据源不存在");
        }
        if (this.execute == null || this.execute.isShutdown()) {
            this.execute = Executors.newSingleThreadExecutor();
        }
        this.executeFuture = this.execute.submit(() -> {
            MDC.put(Constant.FLOW_CODE, this.getFlowCode());
            MDC.put(Constant.REQUEST_ID, context.getId());
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            RLock lock = this.redissonClient.getLock(RedisKey.FLOW_DEBEZIUM_LOCK.build(this.getKey()));
            try {
                RBucket<FlowComponentOnly> flowComponentOnlyRBucket;
                if (!lock.tryLock()) {
                    log.info("数据监听:{} 已经被其他实例抢占并执行,当前实例:{}", this.getKey(), this.serverManager.instanceId());
                    // 未获取到锁的实例跳过
                    return;
                } else {
                    log.info("数据监听获取锁:{},实例:{}", this.getKey(), this.serverManager.instanceId());
                    flowComponentOnlyRBucket = this.redissonClient.getBucket(RedisKey.FLOW_COMPONENT_ONLY.build(this.getKey()));
                    // 判断是否是否允许当前实例执行
                    if (!this.canAssumeDuty(flowComponentOnlyRBucket)) {
                        return;
                    }
                }
                // 标记被当前节点执行
                FlowComponentOnly flowComponentOnly = new FlowComponentOnly();
                flowComponentOnly.setInstanceId(this.serverManager.instanceId());
                flowComponentOnly.setStartTime(LocalDateTime.now());
                flowComponentOnlyRBucket.set(flowComponentOnly);
                // 注册心跳
                this.registerHeartBeat(this.serverManager.instanceId(), contextMap);
                // 开始启动
                Configuration configuration = this.getConfiguration(context, source);
                ChangeConsumer changeConsumer = new ChangeConsumer(this, context, contextMap);
                this.debeziumEngine = DebeziumEngine.create(
                                // ChangeEventFormat.of(Connect.class)
                                // 修改为AsyncEmbeddedEngine
                                KeyValueHeaderChangeEventFormat.of(Connect.class, Connect.class, Connect.class),
                                "io.debezium.embedded.async.ConvertingAsyncEngineBuilderFactory"
                        )
                        .using(configuration.asProperties())
                        .using((success, message, throwable) -> {
                            Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
                            if (CollUtil.isEmpty(copyOfContextMap)) {
                                MDC.setContextMap(contextMap);
                            }
                            if (!success) {
                                log.error("数据监听执行异常中断:" + message, throwable);
                                // bug修复
                                if (throwable == null) {
                                    throwable = new Exception(message);
                                }
                                // 异常处理
                                this.exception(throwable, FlowError.ErrorType.ABORT);
                            }
                        })
                        .using(new DebeziumEngine.ConnectorCallback() {

                            @Override
                            public void connectorStarted() {
                                Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
                                if (CollUtil.isEmpty(copyOfContextMap)) {
                                    MDC.setContextMap(contextMap);
                                }
                            }

                            @Override
                            public void taskStarted() {
                                Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
                                if (CollUtil.isEmpty(copyOfContextMap)) {
                                    MDC.setContextMap(contextMap);
                                }
                            }

                            @Override
                            public void connectorStopped() {
                                Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
                                if (CollUtil.isEmpty(copyOfContextMap)) {
                                    MDC.setContextMap(contextMap);
                                }
                                log.warn("数据监听连接器已停止");
                            }

                            @Override
                            public void taskStopped() {
                                // 修复
                                Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
                                if (CollUtil.isEmpty(copyOfContextMap)) {
                                    MDC.setContextMap(contextMap);
                                }
                                log.warn("数据监听任务已停止");
                            }
                        })
                        .notifying(changeConsumer)
                        .build();
                log.info("数据监听开始启动");
                // 内部停止时会让当前线程中断
                this.debeziumEngine.run();
                log.info("数据监听执行结束");
            } catch (Exception e) {
                log.error("数据监听执行失败", e);
                this.exception(e, FlowError.ErrorType.STARTUP);
            } finally {
                // 释放锁
                try {
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        log.info("数据监听释放锁:{}", this.getKey());
                        lock.unlock();
                    }
                } catch (Exception e) {
                    // 释放锁，或者移除心跳数据异常
                    log.warn("数据监听释放锁或移除心跳数据异常:" + this.getKey(), e);
                }
                MDC.clear();
            }
        });
    }

    /**
     * 获取配置构建器
     *
     * @param context 上下文
     * @param source  数据源
     * @return 返回配置构建器
     */
    private Configuration getConfiguration(Context context, Source source) {
        int threads = this.getThreads();
        log.info("数据监听:{} 快照使用最大线程数:{}", this.getKey(), threads);
        // 必须唯一,否则 JmxUtils  Unable to unregister metrics MBean ...
        // 修复组件编码重复导致报错，加上工作空间以及数据流编码
        String topic = "dp-flow-topic-" + this.getKey();
        String name = "dp-flow-connector-" + this.getKey();
        String schemaHistoryInternal = "dp-flow-schema-history-internal-" + this.getKey();
        // 放在前置设置，空属性等异常即时提示出去，不用单独线程内部执行
        // io.debezium.config.CommonConnectorConfig
        Configuration.Builder builder = Configuration.create()
                .with("workspace.code", this.getWorkspaceCode())
                .with("flow.code", this.getFlowCode())
                .with("task.code", this.getCode())
                .with(Constant.REQUEST_ID, context.getId())
                // to MySQLSchemaHistory
                .with("schema.history.internal.key", this.getKey())
                // 启动保存点策略
                .with("start.strategy", this.startStrategy.name())
                .with("topic.prefix", topic)
                // 偏移量持久化,用来容错
                .with("offset.storage", MySQLOffsetBackingStore.class)
                // 捕获偏移量的周期
                .with("offset.flush.interval.ms", this.savePointInterval)
                // 保留时间
                .with("offset.storage.save.point.duration", this.savePointDuration)
                // 设置延迟间隔有助于防止在快照完成后、流式传输进程开始之前发生故障时连接器重新启动快照。
                .with("streaming.delay.ms", this.savePointInterval + 1000)
                // 连接器的唯一名称
                .with("name", name)
                .with("table.include.list", this.tables)
                .with("database.connectionTimeZone", this.connectionTimeZone)
                .with("max.batch.size", this.maxBatchSize)
                // 配置阻塞队列
                // Connector configuration is not valid. The 'max.queue.size' value is invalid: Must be larger than the maximum batch size
                // 始终保持 1 比 4，页面最多配置2w，队列最多8w
                .with("max.queue.size", this.maxBatchSize * 4)
                .with("converters", "dateConverters,binaryConverters")
                .with("dateConverters.type", DateTimeConverter.class)
                .with("dateConverters.timezone", this.connectionTimeZone)
                .with("binaryConverters.type", BinaryConverter.class)
                .with("decimal.handling.mode", "double")
                //.with("binary.handling.mode", "hex")
                // 是否包含schema变更
                .with("include.schema.changes", false)
                // MySQL服务器或集群的逻辑名称
                .with("schema.history.internal.name", schemaHistoryInternal)
                //.with("database.server.name", "dp-flow-server-" + this.getKey())
                // 历史变更记录
                .with("schema.history.internal", MySQLSchemaHistory.class.getName())
                // 只会保存与那些被 Debezium 明确配置要捕获变更的数据库相关的 DDL 信息,而忽略其他数据库的 DDL 信息。
                .with("schema.history.internal.store.only.captured.databases.ddl", "true")
                // 只会存储那些明确被配置为要捕获变更的表的 DDL（数据定义语言,例如 CREATE TABLE、ALTER TABLE 等操作）语句。
                // 而对于其他未被配置捕获的表的 DDL 语句,不会被记录到 Schema 历史中。
                .with("schema.history.internal.store.only.captured.tables.ddl", "true")
                // 使用无锁模式
                .with("snapshot.locking.mode", "none")
                // snapshot.max.threads
                .with("snapshot.max.threads", threads)
                // https://issues.redhat.com/browse/DBZ-7342
                .with("errors.max.retries", 3)
                // 如果没有数据流动，多久发送一次心跳
                // 解决自动模式存在问题  当存量同步完，过程或者以后没有任何变更时，没有后续保存点了，重启后将会再次触发存量
                .with("heartbeat.interval.ms", 30 * 1000)
                // 指定连接器在binlog事件反序列化期间应如何对异常做出反应。
                // warn记录有问题的事件及其binlog偏移量，然后跳过该事件。
                .with("event.processing.failure.handling.mode", "warn")
                // 指定连接器应如何响应与内部架构表示中不存在的表相关的 binlog 事件。也就是说，内部表示与数据库不一致。
                // warn记录有问题的事件及其 binlog 偏移量并跳过该事件。
                .with("inconsistent.schema.handling.mode", "warn")
                // .with("signal.enabled.channels", "file")
                ;
        // 配置连接器
        ConnectorConfigure connectorConfigure = ConnectorConfigure.get(source);
        connectorConfigure.configure(this, source, builder);
        if (StartStrategy.CUSTOM.equals(this.startStrategy)) {
            if (this.savePoint != null) {
                // 查询是否存在
                builder.with("offset.storage.save.point", this.savePoint);
            } else {
                throw new RuntimeException("未设置保存点");
            }
        }
        // 如果选择了恢复模式优先级高
        if (this.startStrategy.equals(StartStrategy.RECOVERY)) {
            builder.with("snapshot.mode", "recovery");
            log.info("数据监听:{} 设置为恢复模式", this.getKey());
        } else if (!this.operations.contains(Envelope.Operation.READ)) {
            // 去除INCREASE选项，当用户没有选择操作类型为READ时，默认就是不读取历史数据，简化使用
            // 不会在启动时生成存量数据的快照
            builder.with("snapshot.mode", "no_data");
            log.info("数据监听:{} 设置为不进行初始快照", this.getKey());
        }
        // 按照默认snapshot.mode=initial执行
        // 自定义SQL
        // snapshot.select.statement.overrides=inventory.products,inventory.customers
        // snapshot.select.statement.overrides.inventory.products=SELECT * FROM inventory.products WHERE category='electronics'
        // snapshot.select.statement.overrides.inventory.customers=SELECT id, name, email FROM inventory.customers WHERE status='active'
        // 自定义配置覆盖默认配置
        if (CollUtil.isNotEmpty(this.properties)) {
            this.properties.forEach((k, v) -> builder.with(k.toString(), v.toString()));
        }
        // 打印出当前监听的库以及表
        log.info("数据监听:{} 监听数据库:{} 表:{}", this.getKey(), this.schemas, this.tables);
        return builder.build();
    }

    /**
     * 注册心跳任务
     *
     * @param instanceId    实例ID
     * @param mdcContextMap mdc上下文
     */
    public void registerHeartBeat(String instanceId, Map<String, String> mdcContextMap) {
        this.heartBeatScheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            try {
                MDC.setContextMap(mdcContextMap);
                // 发送心跳
                log.info("数据监听心跳更新:{},当前实例:{}", this.getKey(), instanceId);
                RBucket<Long> bucket = this.redissonClient.getBucket(
                        RedisKey.FLOW_DEBEZIUM_HEARTBEAT.build(this.getKey() + "-" + instanceId));
                // 设置超时，如果超过*秒没有更新，则表示挂了
                bucket.set(System.currentTimeMillis(), Duration.ofSeconds(120));
            } catch (Exception e) {
                log.error("数据监听心跳任务执行失败:" + this.getKey(), e);
            } finally {
                MDC.clear();
            }
        };
        this.heartBeatScheduler.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
    }


    /**
     * 判断是否是否允许当前实例执行，防止后加入节点重复执行
     *
     * @param flowComponentOnlyRBucket 流组件运行实例信息
     * @return 返回true时，允许当前实例执行，返回false时，表示其他节点正在执行，当前节点不需要执行
     */
    private boolean canAssumeDuty(RBucket<FlowComponentOnly> flowComponentOnlyRBucket) {
        FlowComponentOnly flowComponentOnly = flowComponentOnlyRBucket.get();
        // 是否健康的
        if (flowComponentOnly != null
                // 如果遇到异常等重启时，正巧当前实例是运行中的实例，不能被直接return出去，直接还是当前节点执行
                && !Objects.equals(flowComponentOnly.getInstanceId(), this.serverManager.instanceId())) {
            // 是否还有心跳
            RBucket<Object> rBucket = this.redissonClient.getBucket(
                    RedisKey.FLOW_DEBEZIUM_HEARTBEAT.build(this.getKey() + "-" + flowComponentOnly.getInstanceId()));
            if (rBucket.isExists()) {
                // 心跳正常，本次不启动
                log.info("数据监听:{} 运行中实例:{} 状态正常,当前实例不启动:{}", this.getKey(),
                        flowComponentOnly.getInstanceId(), this.serverManager.instanceId());
                return false;
            }
            // 已经没有心跳
            log.info("数据监听:{} 运行中实例:{} 心跳已停止,当前实例:{} 开始接管执行", this.getKey(),
                    flowComponentOnly.getInstanceId(), this.serverManager.instanceId());
            // 防止原来的又活过来，例如因那个节点因某种原因暂时不可用导致失活，彻底让原来的节点停止
            DataFlowComponentMessageBody dataFlowComponentMessageBody = new DataFlowComponentMessageBody();
            dataFlowComponentMessageBody.setFlowCode(this.getFlowCode());
            dataFlowComponentMessageBody.setWorkspaceCode(this.getWorkspaceCode());
            dataFlowComponentMessageBody.setComponentCode(this.getCode());
            // 只通知原来运行的实例停止
            dataFlowComponentMessageBody.setInstanceId(flowComponentOnly.getInstanceId());
            // 触发Debezium组件重启
            dataFlowComponentMessageBody.setType(DataFlowComponentMessageBody.Type.STOP);
            // 发送停止消息
            EventPublisher.publishEvent(new DataFlowComponentEvent(dataFlowComponentMessageBody));
            // 允许当前节点执行
            return true;
        } else {
            log.info("数据监听:{} 开始执行,实例:{}", this.getKey(), this.serverManager.instanceId());
        }
        // 允许当前节点执行
        return true;
    }


    /**
     * 告警，并中断数据流
     *
     * @param e         异常
     * @param errorType 异常类型
     */
    private void exception(Throwable e, FlowError.ErrorType errorType) {
        Throwable rootCause = ExceptionUtil.getRootCause(e);
        // 停止数据流
        DataFlowMessageBody dataFlowMessageBody = new DataFlowMessageBody();
        dataFlowMessageBody.setType(DataFlowMessageBody.Type.REMOVE);
        dataFlowMessageBody.setId(this.getFlow().getId());
        dataFlowMessageBody.setWorkspaceCode(this.getWorkspaceCode());
        EventPublisher.publishEvent(new DataFlowEvent(dataFlowMessageBody));
        // 标记异常中断
        this.flowMonitor.errorWithAlarm(this, rootCause, errorType);
    }

    /**
     * 根据同步的表数量，来获取线程数
     *
     * @return 线程数
     */
    private int getThreads() {
        if (this.threadNumber != null) {
            return this.threadNumber;
        }
        int length = this.tables.split(",").length;
        // 如果大于10个表，使用4个线程
        if (length > 10) {
            return 4;
        }
        // 如果大于5个表使用3个线程
        if (length > 5) {
            return 3;
        }
        // 如果大于2个表使用2个线程
        if (length > 2) {
            return 2;
        }
        // 否则使用1个线程
        return 1;
    }

    /**
     * 停止
     */
    @Override
    public synchronized void stop() {
        if (this.debeziumEngine != null) {
            log.info("关闭数据监听:" + this.getKey());
            try {
                this.debeziumEngine.close();
                this.debeziumEngine = null;
                log.info("数据监听关闭完成");
            } catch (IllegalStateException e) {
                // 有可能已经关闭，或者未运行，忽略此异常
                log.warn("关闭数据监听失败:" + e.getMessage());
            } catch (Exception e) {
                log.error("关闭数据监听失败", e);
            }
        }
        // 优化，只允许运行的节点进行移除操作，其他节点调用stop时，不能影响正在运行的节点
        RBucket<FlowComponentOnly> flowComponentOnlyRBucket = this.redissonClient.getBucket(
                RedisKey.FLOW_COMPONENT_ONLY.build(this.getKey())
        );
        FlowComponentOnly flowComponentOnly = flowComponentOnlyRBucket.get();
        if (flowComponentOnly != null) {
            String instanceId = flowComponentOnly.getInstanceId();
            String currentInstanceId = this.serverManager.instanceId();
            log.info("数据监听:{} 停止时运行实例:{},当前实例:{}", this.getKey(), instanceId, currentInstanceId);
            // 只有当前运行实例是自己时，才进行移除操作
            if (Objects.equals(instanceId, currentInstanceId)) {
                // 移除运行标识
                try {
                    flowComponentOnlyRBucket.delete();
                    log.info("数据监听:{} 运行状态已移除", this.getKey());
                } catch (Exception e) {
                    log.error("数据监听:{} 运行状态移除失败", this.getKey(), e);
                }
            }
        }
        // 关闭心跳任务
        if (this.heartBeatScheduler != null) {
            try {
                log.info("关闭数据监听心跳任务:{}", this.getKey());
                this.heartBeatScheduler.shutdown();
                this.heartBeatScheduler = null;
            } catch (Exception e) {
                log.error("关闭数据监听心跳任务失败", e);
            }
        }
        // 修复这里释放锁导致的bug，当组件触发重启时，多个节点接收到重启消息，各个节点都先调用stop，再次start
        // 导致所有节点都进行了执行
        // 开始关闭线程
        if (this.executeFuture != null) {
            try {
                this.executeFuture.cancel(false);
                this.executeFuture = null;
            } catch (Exception e) {
                log.error("关闭数据监听执行任务失败", e);
            }
        }
        // 关闭线程池
        if (this.execute != null) {
            this.execute.shutdown();
            this.execute = null;
        }
    }

}