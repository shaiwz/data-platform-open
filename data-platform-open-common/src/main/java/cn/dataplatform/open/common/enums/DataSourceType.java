package cn.dataplatform.open.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
    MYSQL("MySQL"),
    DORIS("Doris"),
    KAFKA("Kafka"),
    ELASTIC("Elastic"),
    POSTGRESQL("PostgreSQL"),
    ;

    private final String value;


    /**
     * 根据类型获取枚举
     *
     * @param type t
     * @return DataSourceType
     */
    public static DataSourceType getByType(String type) {
        return switch (type) {
            case "MySQL" -> MYSQL;
            case "Doris" -> DORIS;
            case "Kafka" -> KAFKA;
            case "Elastic" -> ELASTIC;
            case "PostgreSQL" -> POSTGRESQL;
            default -> throw new UnsupportedOperationException("不支持的操作");
        };
    }
}
