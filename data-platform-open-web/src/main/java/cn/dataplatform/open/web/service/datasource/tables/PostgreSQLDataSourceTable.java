package cn.dataplatform.open.web.service.datasource.tables;

import cn.dataplatform.open.common.enums.DataSourceType;
import cn.dataplatform.open.common.util.IoUtils;
import cn.dataplatform.open.web.vo.data.source.SchemaTable;
import cn.dataplatform.open.web.vo.data.source.TableDetail;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL 数据源表信息实现
 *
 * @author dingqianwen
 * @date 2025/5/14
 * @since 1.0.0
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve", "SqlDialectInspection"})
@Component
public class PostgreSQLDataSourceTable implements DataSourceTable {

    private static final String SCHEMA = "schema";
    private static final String TABLE = "table";
    private static final String COMMENT = "comment";

    /**
     * 获取此连接下所有的库
     *
     * @param connect 数据库连接
     * @return 库名列表
     */
    @SuppressWarnings("DuplicatedCode")
    @SneakyThrows
    @Override
    public List<String> schema(Connection connect) {
        String sql = """
                SELECT DISTINCT n.nspname FROM pg_catalog.pg_class c
                LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                WHERE c.relkind = 'r' AND n.nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast')
                AND n.nspname NOT LIKE 'pg_%' ORDER BY n.nspname""";
        PreparedStatement ps = connect.prepareStatement(sql);
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            List<String> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
            return list;
        } finally {
            IoUtils.close(rs, ps);
        }
    }

    /**
     * 获取此连接下库-表列表
     *
     * @param connect    数据库连接
     * @param schemaCode 库名，为空则查所有库下的表；有值则只查该库下的表
     * @param likeTable  模糊查询表，如果为空，查询全部表
     * @param limit      一次最多返回表数量，小于等于0表示不限制
     * @return 库-表列表
     */
    @SuppressWarnings("DuplicatedCode")
    @SneakyThrows
    @Override
    public List<SchemaTable> table(Connection connect, String schemaCode, String likeTable, int limit) {
        StringBuilder query = new StringBuilder("""
                SELECT n.nspname AS schema,
                c.relname AS table,
                pd.description AS comment
                FROM pg_catalog.pg_class c
                LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                LEFT JOIN pg_catalog.pg_description pd ON pd.objoid = c.oid AND pd.objsubid = 0
                WHERE c.relkind = 'r'
                AND n.nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast')
                AND n.nspname NOT LIKE 'pg_%'
                """);
        if (StrUtil.isNotBlank(schemaCode)) {
            query.append(" AND n.nspname = ?");
        }
        if (StrUtil.isNotBlank(likeTable)) {
            query.append(" AND c.relname LIKE ?");
        }
        query.append(" ORDER BY n.nspname, c.relname");
        if (limit > 0) {
            query.append(" LIMIT ?");
        }
        PreparedStatement preparedStatement = connect.prepareStatement(query.toString());
        ResultSet resultSet = null;
        try {
            int idx = 1;
            if (StrUtil.isNotBlank(schemaCode)) {
                preparedStatement.setString(idx++, schemaCode);
            }
            if (StrUtil.isNotBlank(likeTable)) {
                preparedStatement.setString(idx++, "%" + likeTable + "%");
            }
            if (limit > 0) {
                preparedStatement.setInt(idx, limit);
            }
            resultSet = preparedStatement.executeQuery();
            List<SchemaTable> arrayList = new ArrayList<>();
            while (resultSet.next()) {
                SchemaTable schemaTable = new SchemaTable();
                schemaTable.setSchema(resultSet.getString(SCHEMA));
                schemaTable.setTable(resultSet.getString(TABLE));
                schemaTable.setComment(resultSet.getString(COMMENT));
                arrayList.add(schemaTable);
            }
            return arrayList;
        } finally {
            IoUtils.close(resultSet, preparedStatement);
        }
    }

    /**
     * 获取表的详细信息
     *
     * @param connection 数据库连接
     * @param schema     库
     * @param table      表
     * @return 表的详细信息
     */
    @SneakyThrows
    @Override
    public TableDetail tableDetail(Connection connection, String schema, String table) {
        TableDetail tableDetail = new TableDetail();
        PreparedStatement tableInfoStmt = null;
        PreparedStatement columnsStmt = null;
        PreparedStatement pkStmt = null;
        ResultSet tableInfoRs = null;
        ResultSet columnsRs = null;
        ResultSet pkRs = null;
        try {
            // 获取表的基本信息
            // language=text
            tableInfoStmt = connection.prepareStatement(
                    "SELECT pg_catalog.obj_description(c.oid) AS comment " +
                            "FROM pg_catalog.pg_class c " +
                            "LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
                            "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'r'");
            tableInfoStmt.setString(1, schema);
            tableInfoStmt.setString(2, table);
            tableInfoRs = tableInfoStmt.executeQuery();
            if (tableInfoRs.next()) {
                tableDetail.setComment(tableInfoRs.getString("comment"));
            }
            // 获取表的列信息
            columnsStmt = connection.prepareStatement(
                    "SELECT a.attname AS column_name, " +
                            "       pg_catalog.format_type(a.atttypid, a.atttypmod) AS data_type, " +
                            "       NOT a.attnotnull AS is_nullable, " +
                            "       pg_catalog.col_description(a.attrelid, a.attnum) AS column_comment, " +
                            "       COALESCE(pg_catalog.pg_get_expr(d.adbin, d.adrelid), '') AS column_default " +
                            "FROM pg_catalog.pg_attribute a " +
                            "LEFT JOIN pg_catalog.pg_attrdef d ON (a.attrelid = d.adrelid AND a.attnum = d.adnum) " +
                            "WHERE a.attrelid = (SELECT c.oid FROM pg_catalog.pg_class c " +
                            "                   LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
                            "                   WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'r') " +
                            "AND a.attnum > 0 AND NOT a.attisdropped " +
                            "ORDER BY a.attnum");
            columnsStmt.setString(1, schema);
            columnsStmt.setString(2, table);
            columnsRs = columnsStmt.executeQuery();
            List<TableDetail.Column> columns = new ArrayList<>();
            while (columnsRs.next()) {
                TableDetail.Column column = new TableDetail.Column();
                column.setName(columnsRs.getString("column_name"));
                column.setType(columnsRs.getString("data_type"));
                column.setComment(columnsRs.getString("column_comment"));
                column.setNotNull(!columnsRs.getBoolean("is_nullable"));
                column.setDefaultValue(columnsRs.getString("column_default"));
                columns.add(column);
            }
            tableDetail.setColumns(columns);
            // 获取主键信息
            // language=text
            pkStmt = connection.prepareStatement(
                    "SELECT a.attname AS column_name " +
                            "FROM pg_index i " +
                            "JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey) " +
                            "WHERE i.indrelid = (SELECT c.oid FROM pg_catalog.pg_class c " +
                            "                   LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
                            "                   WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'r') " +
                            "AND i.indisprimary");
            pkStmt.setString(1, schema);
            pkStmt.setString(2, table);
            pkRs = pkStmt.executeQuery();
            while (pkRs.next()) {
                String columnName = pkRs.getString("column_name");
                columns.stream()
                        .filter(c -> c.getName().equals(columnName))
                        .forEach(c -> c.setPrimaryKey(true));
            }
        } finally {
            IoUtils.close(pkRs, pkStmt,
                    columnsRs, columnsStmt, tableInfoRs, tableInfoStmt);
        }
        return tableDetail;
    }


    /**
     * 从索引定义中解析列名
     *
     * @param indexDef 索引定义
     */
    private List<String> parseIndexColumns(String indexDef) {
        // 示例索引定义: CREATE INDEX idx_name ON public.table USING btree (col1, col2)
        int start = indexDef.indexOf('(');
        int end = indexDef.indexOf(')');
        if (start > 0 && end > start) {
            String cols = indexDef.substring(start + 1, end);
            return CollUtil.newArrayList(cols.split(",\\s*"));
        }
        return new ArrayList<>();
    }


    /**
     * 获取数据源类型
     *
     * @return 数据源类型
     */
    @Override
    public DataSourceType getDataSourceType() {
        return DataSourceType.POSTGRESQL;
    }

}
