package cn.dataplatform.open.common.enums.flow;

import cn.dataplatform.open.common.vo.flow.Design;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Stream;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/5/17
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum DesignNodeType {

    /**
     * 基础
     */
    JOB("job", "定时任务", Design.Node.Job.class),
    GATHER("gather", "数据汇聚", Design.Node.Gather.class),
    /**
     * 数据处理
     */
    FILTER("filter", "数据过滤", Design.Node.Filter.class),
    MAP("map", "数据转换", Design.Node.Map.class),
    /**
     * 事件监听
     */
    EVENT_DEBEZIUM("eventDebezium", "实时数据监听", Design.Node.Debezium.class),
    /**
     * 数据库读写
     */
    QUERY_MYSQL("queryMySQL", "查询MySQL", Design.Node.QueryMySQL.class),
    WRITE_MYSQL("writeMySQL", "写入MySQL", Design.Node.WriteMySQL.class),
    QUERY_DORIS("queryDoris", "查询Doris", Design.Node.QueryDoris.class),
    WRITE_DORIS("writeDoris", "写入Doris", Design.Node.WriteDoris.class),
    WRITE_ELASTIC("writeElastic", "写入Elastic", Design.Node.WriteElastic.class),
    QUERY_ORACLE("queryOracle", "查询Oracle", Design.Node.QueryOracle.class),
    WRITE_ORACLE("writeOracle", "写入Oracle", Design.Node.WriteOracle.class),
    WRITE_STAR_ROCKS("writeStarRocks", "写入StarRocks", Design.Node.WriteStarRocks.class),
    QUERY_STAR_ROCKS("queryStarRocks", "查询StarRocks", Design.Node.QueryStarRocks.class),
    QUERY_POSTGRES("queryPostgre", "查询PostgreSQL", Design.Node.QueryPostgreSQL.class),
    WRITE_POSTGRES("writePostgre", "写入PostgreSQL", Design.Node.WritePostgreSQL.class),
    QUERY_DAMENG("queryDameng", "查询达梦", Design.Node.QueryDameng.class),
    WRITE_DAMENG("writeDameng", "写入达梦", Design.Node.WriteDameng.class),
    QUERY_CLICKHOUSE("queryClickHouse", "查询ClickHouse", Design.Node.QueryClickHouse.class),
    /**
     * 消息队列
     */
    KAFKA_SEND("kafkaSend", "Kafka发送", Design.Node.KafkaSend.class),
    KAFKA_RECEIVE("kafkaReceive", "Kafka接收", Design.Node.KafkaReceive.class),
    ROCKET_SEND("rocketSend", "RocketMQ发送", Design.Node.RocketSend.class),
    ROCKET_RECEIVE("rocketReceive", "RocketMQ接收", Design.Node.RocketReceive.class),
    RABBIT_SEND("rabbitSend", "RabbitMQ发送", Design.Node.RabbitSend.class),
    RABBIT_RECEIVE("rabbitReceive", "RabbitMQ接收", Design.Node.RabbitReceive.class),
    /**
     * 其他
     */
    HTTP_PUSH("httpPush", "HTTP推送", Design.Node.HttpPush.class),
    PRINT_LOG("printLog", "打印日志", Design.Node.PrintLog.class),
    ;
    private final String type;
    private final String name;
    private final Class<? extends Design.Node.Properties> propertiesClass;

    private final static Map<String, DesignNodeType> TYPE_MAP = Stream.of(DesignNodeType.values())
            .collect(java.util.stream.Collectors.toMap(DesignNodeType::getType, e -> e));

    /**
     * 根据类型获取枚举
     *
     * @param type t
     * @return DesignNodeType
     */
    public static DesignNodeType getByType(String type) {
        return TYPE_MAP.get(type);
    }

    /**
     * 获取节点类型
     *
     * @param properties json 数据
     * @return Design.Node.Body
     */
    public Design.Node.Properties getProperties(String properties) {
        if (StrUtil.isBlank(properties)) {
            return null;
        }
        return JSON.parseObject(properties, this.propertiesClass);
    }

}
