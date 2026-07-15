package cn.dataplatform.open.flow.service.impl;

import cn.dataplatform.open.common.alarm.scene.DataFlowStartFailNoticeScene;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.enums.RedisKey;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.enums.flow.*;
import cn.dataplatform.open.common.event.AlarmSceneEvent;
import cn.dataplatform.open.common.source.SourceManager;
import cn.dataplatform.open.common.vo.flow.Design;
import cn.dataplatform.open.common.vo.flow.FlowError;
import cn.dataplatform.open.flow.service.core.Flow;
import cn.dataplatform.open.flow.service.core.FlowEngine;
import cn.dataplatform.open.flow.service.core.component.FlowComponent;
import cn.dataplatform.open.flow.service.core.component.JobFlowComponent;
import cn.dataplatform.open.flow.service.core.component.PrintLogFlowComponent;
import cn.dataplatform.open.flow.service.core.component.RateLimitFlowComponent;
import cn.dataplatform.open.flow.service.core.component.event.DebeziumFlowComponent;
import cn.dataplatform.open.flow.service.core.component.event.StartStrategy;
import cn.dataplatform.open.flow.service.core.component.query.MySQLQueryFlowComponent;
import cn.dataplatform.open.flow.service.core.component.write.JDBCWriteTableFlowComponent;
import cn.dataplatform.open.flow.service.core.component.write.MySQLWriteTableFlowComponent;
import cn.dataplatform.open.flow.service.core.monitor.FlowMonitor;
import cn.dataplatform.open.flow.service.core.record.Record;
import cn.dataplatform.open.flow.service.DataFlowPublishService;
import cn.dataplatform.open.flow.store.entity.DataFlowPublish;
import cn.dataplatform.open.flow.store.mapper.DataFlowPublishMapper;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.debezium.data.Envelope;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/22
 * @since 1.0.0
 */
@Order(10)
@Slf4j
@Service
public class DataFlowPublishServiceImpl extends ServiceImpl<DataFlowPublishMapper, DataFlowPublish>
        implements DataFlowPublishService, ApplicationListener<ServletWebServerInitializedEvent> {

    /**
     * 是否在服务启动时加载数据流程，线上需要打开
     * <p>
     * 本地不建议开，否则跑一批数据流任务
     */
    @Value("${dp.flow.start-load:false}")
    private Boolean startLoadFlow;

    @Resource
    private FlowEngine flowEngine;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private SourceManager sourceManager;
    @Resource
    private FlowMonitor flowMonitor;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 启动时加载所有的数据流程
     *
     * @param event event
     */
    @Override
    public void onApplicationEvent(@NonNull ServletWebServerInitializedEvent event) {
        if (!this.startLoadFlow) {
            return;
        }
        List<DataFlowPublish> dataFlowPublishes = this.lambdaQuery()
                .eq(DataFlowPublish::getStatus, Status.ENABLE.name())
                .list();
        if (CollUtil.isEmpty(dataFlowPublishes)) {
            return;
        }
        for (DataFlowPublish dataFlowPublish : dataFlowPublishes) {
            try {
                log.info("加载数据流程:{}", dataFlowPublish.getName());
                // 服务启动时，先清除对应数据流历史报错
                RList<FlowError> flowErrors = this.redissonClient.getList(RedisKey.FLOW_ERROR
                        .build(dataFlowPublish.getWorkspaceCode() + "-" + dataFlowPublish.getCode()));
                flowErrors.delete();
                // 加载数据流
                this.load(dataFlowPublish);
            } catch (Exception e) {
                log.warn("加载数据流程失败:{}", dataFlowPublish.getName(), e);
            }
        }
    }

    /**
     * 加载数据流程
     *
     * @param id 数据流程
     */
    @Override
    public void load(Long id) {
        DataFlowPublish dataFlowPublish = this.getById(id);
        if (dataFlowPublish == null) {
            throw new RuntimeException("数据流程不存在");
        }
        this.load(dataFlowPublish);
    }

    /**
     * 停止数据流程
     *
     * @param id 数据流程ID
     */
    @Override
    public void stop(Long id) {
        DataFlowPublish dataFlowPublish = this.getById(id);
        if (dataFlowPublish == null) {
            throw new RuntimeException("数据流程不存在");
        }
        this.flowEngine.removeFlow(dataFlowPublish.getWorkspaceCode(), dataFlowPublish.getCode());
    }

    /**
     * 加载数据流程
     *
     * @param dataFlowPublish 数据流程
     */
    private void load(DataFlowPublish dataFlowPublish) {
        MDC.put(Constant.FLOW_CODE, dataFlowPublish.getCode());
        try {
            String flowDesign = dataFlowPublish.getDesign();
            Design design = JSON.parseObject(flowDesign, Design.class);
            List<Design.Node> nodes = design.getNodes();
            Map<String, Design.Node> nodeMap = nodes.stream().collect(Collectors.toMap(Design.Node::getId, Function.identity()));

            List<Design.Edge> edges = design.getEdges();
            // 所有的下一个节点
            Set<String> strings = edges.stream().map(Design.Edge::getTargetNodeId).collect(Collectors.toSet());
            // 找到所有的根节点
            // 当前节点不在下一个节点中的,则没有被别人引用,则为根节点
            List<Design.Edge> roots = edges.stream().filter(edge -> !strings.contains(edge.getSourceNodeId())).toList();
            // 分组
            // SourceNodeId 对应多个 targetNodeId
            Map<String, List<Design.Edge>> edgesGroup = edges.stream().collect(Collectors.groupingBy(
                    Design.Edge::getSourceNodeId,
                    Collectors.mapping(Function.identity(), Collectors.toList())));
            Flow flow = new Flow();
            flow.setId(dataFlowPublish.getId());
            flow.setCode(dataFlowPublish.getCode());
            flow.setName(dataFlowPublish.getName());
            flow.setVersion(dataFlowPublish.getVersion());
            if (dataFlowPublish.getEnableMonitor() != null) {
                flow.setEnableMonitor(Objects.equals(Status.ENABLE, Status.valueOf(dataFlowPublish.getEnableMonitor())));
            }
            if (dataFlowPublish.getEnableAlarm() != null) {
                flow.setEnableAlarm(Objects.equals(Status.ENABLE, Status.valueOf(dataFlowPublish.getEnableAlarm())));
            }
            DataFlowRunStrategy runStrategy;
            if (dataFlowPublish.getRunStrategy() == null) {
                // 针对历史数据，默认全部节点运行
                runStrategy = DataFlowRunStrategy.ALL_INSTANCES;
            } else {
                runStrategy = DataFlowRunStrategy.valueOf(dataFlowPublish.getRunStrategy());
            }
            flow.setRunStrategy(runStrategy);
            flow.setInstanceNumber(dataFlowPublish.getInstanceNumber());
            String specifyInstances = dataFlowPublish.getSpecifyInstances();
            if (StrUtil.isNotBlank(specifyInstances)) {
                flow.setSpecifyInstances(JSON.parseArray(specifyInstances, String.class));
            }
            flow.setWorkspaceCode(dataFlowPublish.getWorkspaceCode());
            for (Design.Edge root : roots) {
                String sourceNodeId = root.getSourceNodeId();
                this.recursionFlowComponent(null, nodeMap, edgesGroup, sourceNodeId, flow);
            }
            // 如果原来已经存在
            this.flowEngine.removeFlow(dataFlowPublish.getWorkspaceCode(), flow.getCode());
            // 注册到引擎中
            this.flowEngine.addFlow(flow);
            // 后续由调度器进行统一调度
            log.info("加载数据流程成功,待调度执行:{}-{}({})", dataFlowPublish.getWorkspaceCode(),
                    dataFlowPublish.getCode(), dataFlowPublish.getName());
        } catch (Exception e) {
            log.error("加载数据流程失败:{}-{}({})", dataFlowPublish.getWorkspaceCode(), dataFlowPublish.getCode(), dataFlowPublish.getName(), e);
            // 解析异常标记启动失败
            this.flowMonitor.error(dataFlowPublish.getWorkspaceCode(), dataFlowPublish.getCode(), e, FlowError.ErrorType.STARTUP);
            if (Objects.equals(dataFlowPublish.getEnableAlarm(), Status.ENABLE.name())) {
                DataFlowStartFailNoticeScene noticeScene = new DataFlowStartFailNoticeScene(e);
                noticeScene.setFlowName(dataFlowPublish.getName());
                noticeScene.setFlowCode(dataFlowPublish.getCode());
                this.applicationEventPublisher.publishEvent(new AlarmSceneEvent(dataFlowPublish.getWorkspaceCode(), noticeScene));
            }
        } finally {
            MDC.remove(Constant.FLOW_CODE);
        }
    }


    /**
     * 递归构建流程组件
     *
     * @param component    组件
     * @param nodeMap      节点
     * @param edgesGroup   边
     * @param sourceNodeId 节点ID
     * @param flow         流程
     * @return FlowComponent
     */
    @SneakyThrows
    private FlowComponent recursionFlowComponent(FlowComponent component, Map<String, Design.Node> nodeMap,
                                                 Map<String, List<Design.Edge>> edgesGroup, String sourceNodeId, Flow flow) {
        if (component != null) {
            // 拿到当前节点下所有子节点
            // targetNodeId
            List<Design.Edge> child = edgesGroup.get(sourceNodeId);
            if (CollUtil.isNotEmpty(child)) {
                // 按照Design.Edge.Properties.order 排序，越小越先执行，相同优先级一起执行
                // 转为List<List<Design.Edge>> 按照优先级排序
                List<List<Design.Edge>> priorityEdges = child.stream()
                        // 先按照优先级分组
                        .collect(Collectors.groupingBy(edge -> edge.getProperties().getOrder()))
                        .values().stream()
                        .sorted(Comparator.comparingInt(m ->
                                // 相同优先级的，获取到具体order外层排序
                                m.getFirst().getProperties().getOrder()))
                        .toList();
                // 按照优先级从小到大排序后的循环
                for (List<Design.Edge> priorityEdge : priorityEdges) {
                    // 相同优先级节点
                    List<FlowComponent> flowComponents = new ArrayList<>();
                    for (Design.Edge edge : priorityEdge) {
                        String targetNodeId = edge.getTargetNodeId();
                        FlowComponent flowComponent = this.recursionFlowComponent(null, nodeMap, edgesGroup, targetNodeId, flow);
                        if (flowComponent != null) {
                            flowComponents.add(flowComponent);
                        }
                    }
                    // 添加一组
                    component.addNext(flowComponents);
                }
            }
            return component;
        }
        Design.Node node = nodeMap.get(sourceNodeId);
        if (node == null) {
            throw new RuntimeException("节点不存在:" + sourceNodeId);
        }
        Design.Node.Properties body = node.getProperties();
        String name = body.getName();
        if (StrUtil.isBlank(name)) {
            // 没有输入完整，此节点不执行
            return null;
        }
        Field datasourceCodeField = FieldUtils.getField(body.getClass(), "datasourceCode", true);
        if (datasourceCodeField != null) {
            String datasourceCode = (String) datasourceCodeField.get(body);
            if (this.sourceManager.getSource(flow.getWorkspaceCode(), datasourceCode) == null) {
                throw new RuntimeException("数据源不存在，或未启用:" + flow.getWorkspaceCode() + "-" + datasourceCode);
            }
        }
        switch (body) {
            case Design.Node.Job job -> {
                JobFlowComponent jobFlowComponent = new JobFlowComponent(flow, node.getId());
                jobFlowComponent.setName(job.getName());
                jobFlowComponent.setCron(job.getCron());
                jobFlowComponent.setBlockStrategy(JobFlowComponent.BlockStrategy.valueOf(job.getBlockStrategy()));
                jobFlowComponent.setStatus(job.getStatus());
                // 拿到当前节点下所有子节点
                this.recursionFlowComponent(jobFlowComponent, nodeMap, edgesGroup, sourceNodeId, flow);
                return flow.addFlowComponent(jobFlowComponent);
            }
            case Design.Node.QueryMySQL queryMySQL -> {
                MySQLQueryFlowComponent mySQLQueryFlowComponent = new MySQLQueryFlowComponent(flow, node.getId());
                mySQLQueryFlowComponent.setName(queryMySQL.getName());
                mySQLQueryFlowComponent.setDatasourceCode(queryMySQL.getDatasourceCode());
                mySQLQueryFlowComponent.setSelectText(queryMySQL.getSelectText());
                mySQLQueryFlowComponent.setQueryTimeout(queryMySQL.getQueryTimeout());
                // 流式查询
                if (Objects.equals(queryMySQL.getQueryType(), QueryType.CURSOR.getCode())) {
                    mySQLQueryFlowComponent.setStreamQuery(true);
                } else {
                    mySQLQueryFlowComponent.setScrollColumn(queryMySQL.getScrollColumn());
                    mySQLQueryFlowComponent.setStreamQuery(false);
                }
                mySQLQueryFlowComponent.setLimit(queryMySQL.getLimit());
                // 拿到当前节点下所有子节点
                this.recursionFlowComponent(mySQLQueryFlowComponent, nodeMap, edgesGroup, sourceNodeId, flow);
                return flow.addFlowComponent(mySQLQueryFlowComponent);
            }
            case Design.Node.WriteMySQL writeMySQL -> {
                MySQLWriteTableFlowComponent mySQLWriteTableFlowComponent = new MySQLWriteTableFlowComponent(flow, node.getId());
                mySQLWriteTableFlowComponent.setName(writeMySQL.getName());
                mySQLWriteTableFlowComponent.setDatasourceCode(writeMySQL.getDatasourceCode());
                mySQLWriteTableFlowComponent.setSchema(writeMySQL.getSchemaCode());
                mySQLWriteTableFlowComponent.setTable(writeMySQL.getTableCode());
                Design.CustomSQL customSQL = writeMySQL.getCustomSQL();
                if (customSQL != null && customSQL.isEnable()) {
                    mySQLWriteTableFlowComponent.setCustomSQL(this.convertCustomSQL(customSQL));
                }
                mySQLWriteTableFlowComponent.setPrimaryKey(writeMySQL.getPrimaryKey());
                // 拿到当前节点下所有子节点
                this.recursionFlowComponent(mySQLWriteTableFlowComponent, nodeMap, edgesGroup, sourceNodeId, flow);
                return flow.addFlowComponent(mySQLWriteTableFlowComponent);
            }
            case Design.Node.Debezium debezium -> {
                DebeziumFlowComponent debeziumFlowComponent = new DebeziumFlowComponent(flow, node.getId());
                debeziumFlowComponent.setName(debezium.getName());
                debeziumFlowComponent.setDatasourceCode(debezium.getDatasourceCode());
                List<String> schemas = debezium.getSchemas();
                debeziumFlowComponent.setSchemas(String.join(",", schemas));
                debeziumFlowComponent.setTables(debezium.getTables());
                // properties
                LinkedHashMap<String, String> properties = debezium.getProperties();
                if (CollUtil.isNotEmpty(properties)) {
                    Properties props = new Properties();
                    props.putAll(properties);
                    debeziumFlowComponent.setProperties(props);
                }
                debeziumFlowComponent.setStatus(debezium.getStatus());
                debeziumFlowComponent.setOperations(debezium.getOperations().stream()
                        .map(Envelope.Operation::valueOf).collect(Collectors.toList()));
                debeziumFlowComponent.setSavePoint(debezium.getSavePoint());
                debeziumFlowComponent.setStartStrategy(StartStrategy.valueOf(debezium.getStartStrategy()));
                debeziumFlowComponent.setSavePointDuration(debezium.getSavePointDuration());
                debeziumFlowComponent.setSavePointInterval(debezium.getSavePointInterval());
                debeziumFlowComponent.setDatabaseServerId(debezium.getDatabaseServerId());
                debeziumFlowComponent.setMaxBatchSize(debezium.getMaxBatchSize());
                debeziumFlowComponent.setListenerServerName(debezium.getListenerServerName());
                debeziumFlowComponent.setThreadNumber(debezium.getThreadNumber());
                debeziumFlowComponent.setConnectionTimeZone(debezium.getConnectionTimeZone());
                // 拿到当前节点下所有子节点
                this.recursionFlowComponent(debeziumFlowComponent, nodeMap, edgesGroup, sourceNodeId, flow);
                return flow.addFlowComponent(debeziumFlowComponent);
            }
            case Design.Node.PrintLog printLog -> {
                PrintLogFlowComponent printLogFlowComponent = new PrintLogFlowComponent(flow, node.getId());
                printLogFlowComponent.setName(printLog.getName());
                printLogFlowComponent.setRecordFieldMaxLength(printLog.getRecordFieldMaxLength());
                printLogFlowComponent.setRecordMaxPrintLine(printLog.getRecordMaxPrintLine());
                // 拿到当前节点下所有子节点
                this.recursionFlowComponent(printLogFlowComponent, nodeMap, edgesGroup, sourceNodeId, flow);
                return flow.addFlowComponent(printLogFlowComponent);
            }
            case Design.Node.KafkaSend kafkaSend -> {
                throw new UnsupportedOperationException("开源版本暂不支持");
            }
            case Design.Node.KafkaReceive kafkaReceive -> {
                throw new UnsupportedOperationException("开源版本暂不支持");
            }
            case Design.Node.RateLimit rateLimit -> {
                RateLimitFlowComponent rateLimitFlowComponent = new RateLimitFlowComponent(flow, node.getId());
                rateLimitFlowComponent.setLimit(rateLimit.getLimit());
                rateLimitFlowComponent.setRefreshInterval(rateLimit.getRefreshInterval());
                rateLimitFlowComponent.setChronoUnit(rateLimit.getChronoUnit());
                rateLimitFlowComponent.setName(rateLimit.getName());
                rateLimitFlowComponent.setStatus(rateLimit.getStatus());
                this.recursionFlowComponent(rateLimitFlowComponent, nodeMap, edgesGroup, sourceNodeId, flow);
                return flow.addFlowComponent(rateLimitFlowComponent);
            }
            default -> {
            }
        }
        throw new UnsupportedOperationException("暂不支持:" + body);
    }

    /**
     * 转换自定义SQL
     *
     * @param customSQL 自定义SQL
     * @return 转换后SQL
     */
    private Map<Record.Op, JDBCWriteTableFlowComponent.CustomSQL> convertCustomSQL(Design.CustomSQL customSQL) {
        Map<String, String> mapping = customSQL.getMapping();
        if (CollUtil.isEmpty(mapping)) {
            return Collections.emptyMap();
        }
        Map<Record.Op, JDBCWriteTableFlowComponent.CustomSQL> newCustomSQL = new LinkedHashMap<>(mapping.size());
        Set<String> strings = mapping.keySet();
        for (String op : strings) {
            String script = mapping.get(op);
            if (StrUtil.isBlank(script)) {
                continue;
            }
            JDBCWriteTableFlowComponent.CustomSQL cs = new JDBCWriteTableFlowComponent.CustomSQL(script);
            newCustomSQL.put(Record.Op.valueOf(op), cs);
        }
        return newCustomSQL;
    }


}
