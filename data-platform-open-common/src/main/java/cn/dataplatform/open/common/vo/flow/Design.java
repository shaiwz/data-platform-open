package cn.dataplatform.open.common.vo.flow;

import cn.dataplatform.open.common.enums.flow.DesignNodeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/11
 * @since 1.0.0
 */
@Data
public class Design {

    @Valid
    private List<Node> nodes;

    @Valid
    private List<Edge> edges;

    @Data
    public static class Node {
        @NotBlank(message = "节点编码不能为空")
        private String id;
        @NotBlank(message = "节点类型不能为空")
        private String type;
        private String properties;

        /**
         * 获取节点属性
         *
         * @return Properties
         */
        public Properties getProperties() {
            if (this.type == null) {
                throw new IllegalArgumentException("type is null");
            }
            DesignNodeType designNodeType = DesignNodeType.getByType(this.type);
            if (designNodeType == null) {
                throw new IllegalArgumentException("type is not support");
            }
            return designNodeType.getProperties(this.properties);
        }

        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class WriteElastic extends Properties {
            @NotBlank(message = "写入Elastic数据源不能为空")
            private String datasourceCode;
            @NotBlank(message = "写入Elastic索引不能为空")
            private String index;
            private List<String> primaryKey;
        }

        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class KafkaReceive extends Properties {
            @NotBlank(message = "Kafka消费数据源不能为空")
            private String datasourceCode;
            private String group;
            @NotBlank(message = "Kafka消费主题不能为空")
            private String topic;
            private String key;
            @NotBlank(message = "Kafka消费状态不能为空")
            private String status;
            /**
             * 是否开启幂等判断
             */
            private Boolean idempotent;
            /**
             * 幂等持有时间 单位分钟
             */
            private Integer idempotentDuration = 60 * 60;

            private String idempotentScope;

            /**
             * 是否开启批量消费模式
             */
            private Boolean batch = false;
            /**
             * 批量消费的数量
             * <p>
             * 如果消息内容已经是批量记录
             * <p>
             * 则往下游推送数量为  批量记录数量 * 批量消费的数量
             */
            @NotNull
            private Integer batchSize = 100;
            /**
             * 超时时间 ms
             * 0 表示不启用超时
             */
            private int batchReceiveTimeout = 1000;
        }


        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class KafkaSend extends Properties {
            @NotBlank(message = "Kafka发送数据源不能为空")
            private String datasourceCode;
            @NotBlank(message = "Kafka发送主题不能为空")
            private String topic;
            private String key;
            @NotNull
            private Integer batchSplitSize = 100;
        }


        /**
         * DorisWriteTableFlowComponent
         */
        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class WriteDoris extends Properties {
            @NotBlank(message = "写入Doris数据源不能为空")
            private String datasourceCode;
            @NotBlank(message = "写入Doris数据库不能为空")
            private String schemaCode;
            @NotBlank(message = "写入Doris表不能为空")
            private String tableCode;
        }

        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class Map extends Properties {
            private Boolean retainOriginalField;

            private List<KeyValue> fieldMapping;
            private List<KeyValue> valueMapping;

        }

        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class Gather extends Properties {
            private Integer gatherCount;
            /**
             * 集群内共享,开启后降低吞吐量
             */
            private Boolean shared = false;
            private Integer timeout;
        }

        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class PrintLog extends Properties {
            /**
             * 记录中字段长度限制，超出截断
             * -1不限制
             */
            private Integer recordFieldMaxLength = 1000;

            /**
             * 记录最大打印行数
             * -1不限制
             */
            private Integer recordMaxPrintLine = 200;
        }

        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class Filter extends Properties {
            /**
             * STREAM_RECORD,PLAIN_RECORD
             */
            @NotBlank(message = "过滤类型不能为空")
            private String filterType;
            private Boolean ifEmptyContinue = false;
            private StreamRecord streamRecord;
            private PlainRecord plainRecord;

            @Data
            public static class StreamRecord {
                private String schema;
                private String table;
                private List<String> operations;
                private List<Condition> conditionBefore;
                private List<Condition> conditionAfter;
            }

            @Data
            public static class PlainRecord {
                private List<Condition> condition;
            }

            @Data
            public static class Condition {
                private String field;
                private String operator;
                private String valueType;
                private Object value;
            }
        }


        @Data
        public static class Properties {
            private String id;
            @NotBlank(message = "组件名称不能为空")
            private String name;
            private String description;
        }

        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class QueryDoris extends Properties {
            @NotBlank(message = "查询Doris数据源不能为空")
            private String datasourceCode;
            @NotBlank(message = "查询Doris查询脚本不能为空")
            private String selectText;
            private String queryType;
            private Long limit;
            /**
             * 滚动查询列
             */
            private String scrollColumn;
            private Integer queryTimeout = 6000;
        }

        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class QueryMySQL extends Properties {
            @NotBlank(message = "查询MySQL数据源不能为空")
            private String datasourceCode;
            @NotBlank(message = "查询MySQL查询脚本不能为空")
            private String selectText;
            private String queryType;
            private Long limit;
            /**
             * 滚动查询列
             */
            private String scrollColumn;
            private Integer queryTimeout = 6000;
        }

        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class WriteMySQL extends Properties {
            @NotBlank(message = "写入MySQL数据源不能为空")
            private String datasourceCode;
            @NotBlank(message = "写入MySQL数据库不能为空")
            private String schemaCode;
            @NotBlank(message = "写入MySQL表不能为空")
            private String tableCode;
            private CustomSQL customSQL;
            @NotNull(message = "写入MySQL主键不能为空")
            private List<String> primaryKey;

            @Data
            public static class CustomSQL {
                private boolean enable;
                private java.util.Map<String, String> mapping;
            }

        }

        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class Job extends Properties {
            @NotBlank(message = "任务触发时间不能为空")
            private String cron;
            @NotBlank(message = "任务状态不能为空")
            private String status;
            @NotBlank(message = "任务阻塞策略不能为空")
            private String blockStrategy;
        }

        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class Debezium extends Properties {
            @NotBlank(message = "Debezium状态不能为空")
            private String status;
            private List<String> schemas;
            @NotBlank(message = "Debezium表不能为空")
            private String tables;
            @NotBlank(message = "Debezium数据源不能为空")
            private String datasourceCode;
            private List<String> operations;
            private String startStrategy;
            private String savePoint;
            /**
             * 保存间隔
             * 毫秒
             */
            private Integer savePointInterval;
            /**
             * 小时
             */
            private Integer savePointDuration;
            private Integer databaseServerId;

            private Integer maxBatchSize;

            private String listenerServerName;

            private LinkedHashMap<String, String> properties;

            /**
             * 线程数
             */
            private Integer threadNumber;
            @NotBlank(message = "Debezium连接时区不能为空")
            private String connectionTimeZone = "GMT+08";
        }


        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class HttpPush extends Properties {
            @NotBlank(message = "HTTP推送请求地址不能为空")
            private String url;
            private java.util.Map<String, String> headers;
            private int connectTimeout = 5000;
            private int readTimeout = 10000;
        }
    }


    @Data
    public static class Edge {
        private String id;

        /**
         * 连线
         */
        private String sourceNodeId;
        private String targetNodeId;

        private Properties properties;

        @Data
        public static class Properties {
            /**
             * 当前节点执行顺序，默认值为0
             */
            private int order = 0;
        }

        /**
         * 获取连线属性
         */
        public Properties getProperties() {
            // 兼容历史的数据
            if (this.properties == null) {
                this.properties = new Properties();
            }
            return this.properties;
        }
    }

}
