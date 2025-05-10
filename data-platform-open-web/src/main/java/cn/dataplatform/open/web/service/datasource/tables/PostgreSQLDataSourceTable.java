package cn.dataplatform.open.web.service.datasource.tables;

import cn.dataplatform.open.web.vo.data.source.SchemaTable;
import cn.dataplatform.open.web.vo.data.source.TableDetail;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author DaoDao
 */
public class PostgreSQLDataSourceTable implements DataSourceTable {

    private static final String SCHEMA = "schema";
    private static final String TABLE = "table";
    private static final String COMMENT = "comment";


    /**
     * 获取数据源下所有的库表
     *
     * @param connect 数据库连接
     * @return 数据库表列表
     */
    @SuppressWarnings("all")
    @SneakyThrows
    @Override
    public List<SchemaTable> schemaTable(Connection connect) {
        // PostgreSQL uses different system tables to store metadata
        String query = """
                SELECT n.nspname AS schema,
                c.relname AS table,
                pd.description AS comment
                FROM pg_catalog.pg_class c
                LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                LEFT JOIN pg_catalog.pg_description pd ON pd.objoid = c.oid AND pd.objsubid = 0
                WHERE c.relkind = 'r' -- // Only tables, not views or other objects
                AND n.nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast')
                AND n.nspname NOT LIKE 'pg_%'
                ORDER BY n.nspname, c.relname
                """;

        List<SchemaTable> arrayList;
        PreparedStatement preparedStatement = connect.prepareStatement(query);
        ResultSet resultSet = null;
        try {
            resultSet = preparedStatement.executeQuery();
            arrayList = new ArrayList<>();
            while (resultSet.next()) {
                SchemaTable schemaTable = new SchemaTable();
                schemaTable.setSchema(resultSet.getString(SCHEMA));
                schemaTable.setTable(resultSet.getString(TABLE));
                schemaTable.setComment(resultSet.getString(COMMENT));
                arrayList.add(schemaTable);
            }
        } finally {
            IoUtil.close(resultSet);
            IoUtil.close(preparedStatement);
        }
        return arrayList;
    }

    /**
     * 获取表的详细信息
     *
     * @param connection 数据库连接
     * @param schema     库
     * @param table      表
     * @return 表的详细信息
     */
    @SuppressWarnings("all")
    @SneakyThrows
    @Override
    public TableDetail tableDetail(Connection connection, String schema, String table) {
        TableDetail tableDetail = new TableDetail();
        PreparedStatement tableInfoStmt = null;
        PreparedStatement columnsStmt = null;
        PreparedStatement pkStmt = null;
        PreparedStatement indexesStmt = null;
        ResultSet tableInfoRs = null;
        ResultSet columnsRs = null;
        ResultSet pkRs = null;
        ResultSet indexesRs = null;
        try {
            // 获取表的基本信息
            tableInfoStmt = connection.prepareStatement(
                    "SELECT pg_catalog.obj_description(c.oid) AS comment," +
                            " (pg_stat_file(pg_relation_filepath(c.oid))).modification AS file_modification_time " +
                            "FROM pg_catalog.pg_class c " +
                            "LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
                            "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'r'");
            tableInfoStmt.setString(1, schema);
            tableInfoStmt.setString(2, table);
            tableInfoRs = tableInfoStmt.executeQuery();
            if (tableInfoRs.next()) {
                tableDetail.setComment(tableInfoRs.getString("comment"));
                tableDetail.setCreateTime(tableInfoRs.getTimestamp("file_modification_time").toLocalDateTime());
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
            // 获取索引信息
            indexesStmt = connection.prepareStatement(
                    "SELECT c2.relname AS index_name, " +
                            "       i.indisunique AS is_unique, " +
                            "       pg_get_indexdef(i.indexrelid, 0, true) AS index_def " +
                            "FROM pg_index i " +
                            "JOIN pg_class c ON c.oid = i.indrelid " +
                            "JOIN pg_class c2 ON c2.oid = i.indexrelid " +
                            "JOIN pg_namespace n ON n.oid = c.relnamespace " +
                            "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'r' " +
                            "AND NOT i.indisprimary");
            indexesStmt.setString(1, schema);
            indexesStmt.setString(2, table);
            indexesRs = indexesStmt.executeQuery();
            Map<String, TableDetail.Index> indexes = new LinkedHashMap<>();
            while (indexesRs.next()) {
                String indexName = indexesRs.getString("index_name");
                ResultSet finalIndexesRs = indexesRs;
                indexes.computeIfAbsent(indexName, k -> {
                    TableDetail.Index newIndex = new TableDetail.Index();
                    newIndex.setName(indexName);
                    try {
                        newIndex.setUnique(finalIndexesRs.getBoolean("is_unique"));
                        newIndex.setColumns(this.parseIndexColumns(finalIndexesRs.getString("index_def")));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return newIndex;
                });
            }
            tableDetail.setIndexes(new ArrayList<>(indexes.values()));
        } finally {
            IoUtil.close(indexesRs);
            IoUtil.close(indexesStmt);
            IoUtil.close(pkRs);
            IoUtil.close(pkStmt);
            IoUtil.close(columnsRs);
            IoUtil.close(columnsStmt);
            IoUtil.close(tableInfoRs);
            IoUtil.close(tableInfoStmt);
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

}
