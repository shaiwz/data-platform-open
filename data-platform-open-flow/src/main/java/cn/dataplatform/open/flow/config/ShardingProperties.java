package cn.dataplatform.open.flow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/15
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "sharding")
public class ShardingProperties {

    private Map<String, Config> datasource;

    @Data
    public static class Config {
        private Map<String, Object> props;
        private Map<String, Table> tables;
        private Map<String, ShardingAlgorithms> shardingAlgorithms;

        @Data
        public static class Table {
            private String actualDataNodes;
            private TableStrategy tableStrategy;

            @Data
            public static class TableStrategy {
                private Standard standard;

                @Data
                public static class Standard {
                    private String shardingColumn;
                    private String shardingAlgorithmName;
                }
            }
        }

        @Data
        public static class ShardingAlgorithms {
            private String type;
            private Map<String, String> props;
        }
    }

}
