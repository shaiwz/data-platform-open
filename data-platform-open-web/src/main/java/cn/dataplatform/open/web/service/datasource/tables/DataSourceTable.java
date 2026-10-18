package cn.dataplatform.open.web.service.datasource.tables;


import cn.dataplatform.open.common.enums.DataSourceType;
import cn.dataplatform.open.web.vo.data.source.SchemaTable;
import cn.dataplatform.open.web.vo.data.source.TableDetail;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/15
 * @since 1.0.0
 */
public interface DataSourceTable {

    /**
     * 获取此连接下所有的库
     *
     * @param connect 数据库连接
     * @return 库名列表
     */
    List<String> schema(Connection connect);

    /**
     * 获取此连接下库-表列表
     *
     * @param connect    数据库连接
     * @param schemaCode 库名，为空则查所有库下的表；有值则只查该库下的表
     * @param likeTable  模糊查询表，如果为空，查询全部表
     * @param limit      一次最多返回表数量，小于等于0表示不限制
     * @return 库-表列表
     */
    List<SchemaTable> table(Connection connect, String schemaCode, String likeTable, int limit);

    /**
     * 获取表的详细信息
     *
     * @param connection 数据库连接
     * @param schema     库
     * @param table      表
     * @return 表的详细信息
     */
    TableDetail tableDetail(Connection connection, String schema, String table);

    /**
     * 获取数据源类型
     *
     * @return 数据源类型
     */
    DataSourceType getDataSourceType();

    /**
     * 获取排除的schema
     *
     * @return 排除的schema
     */
    default List<String> excludedSchema() {
        return List.of();
    }

}
