package cn.dataplatform.open.web.service.datasource.tables;


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
     * 获取此连接下所有的库-表
     *
     * @param connect 数据库连接
     * @return 库-表
     */
    @SneakyThrows
    List<SchemaTable> schemaTable(Connection connect);

    /**
     * 获取表的详细信息
     *
     * @param connection 数据库连接
     * @param schema     库
     * @param table      表
     * @return 表的详细信息
     */
    TableDetail tableDetail(Connection connection, String schema, String table);

}
