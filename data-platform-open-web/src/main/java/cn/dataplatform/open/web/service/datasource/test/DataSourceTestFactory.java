package cn.dataplatform.open.web.service.datasource.test;

import cn.dataplatform.open.common.enums.DataSourceType;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/4
 * @since 1.0.0
 */
public class DataSourceTestFactory {

    /**
     * 获取数据源
     *
     * @param type 数据源类型
     * @return r
     */
    public static DataSourceTest get(String type) {
        DataSourceType dataSourceType = DataSourceType.getByType(type);
        return switch (dataSourceType) {
            case MYSQL -> new MySQLDataSourceTest();
            case DORIS -> new DorisDataSourceTest();
            case ELASTIC -> new ElasticDataSourceTest();
            case RABBIT_MQ -> new RabbitMQDataSourceTest();
            case ROCKET_MQ -> new RocketMQDataSourceTest();
            case ORACLE -> new OracleDataSourceTest();
            case KAFKA -> new KafkaDataSourceTest();
            case POSTGRESQL -> new PostgreSQLDataSourceTest();
            case STAR_ROCKS -> new StarRocksDataSourceTest();
            default -> throw new UnsupportedOperationException("不支持的数据源类型");
        };
    }

}
