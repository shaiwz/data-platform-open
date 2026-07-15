package cn.dataplatform.open.common.enums;

import cn.dataplatform.open.common.source.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum DataSourceType {
    /**
     * MySQL等
     */
    MYSQL("MySQL", MySQLDataSource.class),
    DORIS("Doris", DorisDataSource.class),
    KAFKA("Kafka", KafkaDataSource.class),
    ELASTIC("Elastic", ElasticDataSource.class),
    POSTGRESQL("PostgreSQL", PostgreSQLDataSource.class),
    ;

    private final String value;
    /**
     * 数据源类型对应的class类
     */
    private final Class<? extends Source> sourceClass;

    /**
     * 类型编码映射
     */
    private static final Map<String, DataSourceType> TYPE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(DataSourceType::getValue, e -> e));

    /**
     * 根据类型获取枚举
     *
     * @param type t
     * @return DataSourceType
     */
    public static DataSourceType getByType(String type) {
        DataSourceType dataSourceType = TYPE_MAP.get(type);
        if (dataSourceType == null) {
            throw new UnsupportedOperationException("不支持的数据源类型: " + type);
        }
        return dataSourceType;
    }

    /**
     * 是否为jdbc数据源
     *
     * @return true是jdbc数据源
     */
    public boolean isJdbc() {
        return JDBCSource.class.isAssignableFrom(this.sourceClass);
    }

}
