package cn.dataplatform.open.web.service.datasource.tables;


import cn.dataplatform.open.common.enums.DataSourceType;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/15
 * @since 1.0.0
 */
public class DataSourceTableFactory {


    /**
     * 获取数据源表信息
     *
     * @param type 数据源类型
     * @return r
     */
    public static DataSourceTable get(String type) {
        DataSourceType dataSourceType = DataSourceType.getByType(type);
        return switch (dataSourceType) {
            case MYSQL -> new MySQLDataSourceTable();
            case DORIS -> new DorisDataSourceTable();
            case POSTGRESQL -> new PostgreSQLDataSourceTable();
            default -> throw new UnsupportedOperationException("不支持的数据源类型");
        };
    }

}
