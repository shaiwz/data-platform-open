package cn.dataplatform.open.web.service.datasource.tables;


import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.web.vo.data.source.SchemaTable;
import cn.dataplatform.open.web.vo.data.source.TableDetail;
import cn.hutool.core.io.IoUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/15
 * @since 1.0.0
 */
@Slf4j
public class OracleDataSourceTable implements DataSourceTable {

    /**
     * 获取数据库所有的表
     *
     * @param connect 数据库连接
     * @return r
     */
    @SuppressWarnings("all")
    @Override
    public List<SchemaTable> schemaTable(Connection connect) {
        List<SchemaTable> schemaTables = new ArrayList<>();
        try {
            String sql = "SELECT at.owner, at.table_name, utc.comments FROM all_tables at " +
                    "LEFT JOIN user_tab_comments utc ON at.table_name = utc.table_name WHERE at.owner = USER";
            try (Statement statement = connect.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    String schema = resultSet.getString("owner");
                    String table = resultSet.getString("table_name");
                    String comment = resultSet.getString("comments");
                    schemaTables.add(new SchemaTable(schema, table, comment));
                }
            }
            return schemaTables;
        } catch (SQLException e) {
            log.error("获取数据库表结构失败", e);
            throw new ApiException("获取数据库表结构失败: " + e.getMessage());
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
    @SuppressWarnings("all")
    @SneakyThrows
    @Override
    public TableDetail tableDetail(Connection connection, String schema, String table) {
        TableDetail tableDetail = new TableDetail();
        PreparedStatement tableInfoStmt = null;
        PreparedStatement columnsStmt = null;
        PreparedStatement indexesStmt = null;
        ResultSet tableInfoRs = null;
        ResultSet columnsRs = null;
        ResultSet indexesRs = null;
        try {
            // 获取表的基本信息
            tableInfoStmt = connection.prepareStatement(
                    "SELECT CREATED, LAST_DDL_TIME, COMMENTS " +
                            "FROM ALL_OBJECTS o " +
                            "LEFT JOIN ALL_TAB_COMMENTS c ON o.OWNER = c.OWNER AND o.OBJECT_NAME = c.TABLE_NAME " +
                            "WHERE o.OWNER = ? AND o.OBJECT_NAME = ? AND o.OBJECT_TYPE = 'TABLE'");
            tableInfoStmt.setString(1, schema);
            tableInfoStmt.setString(2, table);
            tableInfoRs = tableInfoStmt.executeQuery();
            if (tableInfoRs.next()) {
                tableDetail.setCreateTime(tableInfoRs.getTimestamp("CREATED").toLocalDateTime());
                tableDetail.setComment(tableInfoRs.getString("COMMENTS"));
            }
            // 获取表的列信息
            columnsStmt = connection.prepareStatement(
                    "SELECT c.COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, DATA_SCALE, " +
                            "       NULLABLE, DATA_DEFAULT, COMMENTS " +
                            "FROM ALL_TAB_COLUMNS c " +
                            "LEFT JOIN ALL_COL_COMMENTS cc ON c.OWNER = cc.OWNER AND c.TABLE_NAME = cc.TABLE_NAME AND c.COLUMN_NAME = cc.COLUMN_NAME " +
                            "WHERE c.OWNER = ? AND c.TABLE_NAME = ? " +
                            "ORDER BY COLUMN_ID");
            columnsStmt.setString(1, schema);
            columnsStmt.setString(2, table);
            columnsRs = columnsStmt.executeQuery();
            List<TableDetail.Column> columns = new ArrayList<>();
            while (columnsRs.next()) {
                TableDetail.Column column = new TableDetail.Column();
                column.setName(columnsRs.getString("COLUMN_NAME"));
                column.setType(this.convertDataType(columnsRs));
                column.setComment(columnsRs.getString("COMMENTS"));
                column.setNotNull("N".equals(columnsRs.getString("NULLABLE")));
                column.setDefaultValue(columnsRs.getString("DATA_DEFAULT"));
                column.setMaxLength(columnsRs.getLong("DATA_LENGTH"));
                columns.add(column);
            }
            tableDetail.setColumns(columns);
            // 获取主键信息
            PreparedStatement pkStmt = null;
            ResultSet pkRs = null;
            try {
                pkStmt = connection.prepareStatement(
                        "SELECT cols.COLUMN_NAME " +
                                "FROM ALL_CONSTRAINTS cons, ALL_CONS_COLUMNS cols " +
                                "WHERE cons.CONSTRAINT_TYPE = 'P' " +
                                "AND cons.CONSTRAINT_NAME = cols.CONSTRAINT_NAME " +
                                "AND cons.OWNER = cols.OWNER " +
                                "AND cons.OWNER = ? " +
                                "AND cols.TABLE_NAME = ? " +
                                "ORDER BY cols.POSITION");
                pkStmt.setString(1, schema);
                pkStmt.setString(2, table);
                pkRs = pkStmt.executeQuery();
                while (pkRs.next()) {
                    String columnName = pkRs.getString("COLUMN_NAME");
                    columns.stream()
                            .filter(c -> c.getName().equals(columnName))
                            .forEach(c -> c.setPrimaryKey(true));
                }
            } finally {
                IoUtil.close(pkRs);
                IoUtil.close(pkStmt);
            }
            // 获取索引信息
            indexesStmt = connection.prepareStatement(
                    "SELECT i.INDEX_NAME, i.UNIQUENESS, ic.COLUMN_NAME, ic.COLUMN_POSITION " +
                            "FROM ALL_INDEXES i, ALL_IND_COLUMNS ic " +
                            "WHERE i.INDEX_NAME = ic.INDEX_NAME " +
                            "AND i.TABLE_OWNER = ic.TABLE_OWNER " +
                            "AND i.TABLE_NAME = ic.TABLE_NAME " +
                            "AND i.TABLE_OWNER = ? " +
                            "AND i.TABLE_NAME = ? " +
                            "ORDER BY i.INDEX_NAME, ic.COLUMN_POSITION");
            indexesStmt.setString(1, schema);
            indexesStmt.setString(2, table);
            indexesRs = indexesStmt.executeQuery();
            Map<String, TableDetail.Index> indexes = new LinkedHashMap<>();
            while (indexesRs.next()) {
                String indexName = indexesRs.getString("INDEX_NAME");
                ResultSet finalIndexesRs = indexesRs;
                TableDetail.Index index = indexes.computeIfAbsent(indexName, k -> {
                    TableDetail.Index newIndex = new TableDetail.Index();
                    newIndex.setName(indexName);
                    try {
                        newIndex.setUnique("UNIQUE".equals(finalIndexesRs.getString("UNIQUENESS")));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    newIndex.setColumns(new ArrayList<>());
                    return newIndex;
                });
                index.addColumn(indexesRs.getString("COLUMN_NAME"));
            }
            tableDetail.setIndexes(new ArrayList<>(indexes.values()));
        } finally {
            IoUtil.close(indexesRs);
            IoUtil.close(indexesStmt);
            IoUtil.close(columnsRs);
            IoUtil.close(columnsStmt);
            IoUtil.close(tableInfoRs);
            IoUtil.close(tableInfoStmt);
        }
        return tableDetail;
    }


    /**
     * 构建Oracle数据类型字符串
     *
     * @param rs ResultSet
     * @return 数据类型字符串
     */
    private String convertDataType(ResultSet rs) throws SQLException {
        String dataType = rs.getString("DATA_TYPE");
        if ("NUMBER".equals(dataType)) {
            int precision = rs.getInt("DATA_PRECISION");
            int scale = rs.getInt("DATA_SCALE");
            if (precision > 0) {
                if (scale > 0) {
                    return String.format("NUMBER(%d,%d)", precision, scale);
                } else {
                    return String.format("NUMBER(%d)", precision);
                }
            }
            return "NUMBER";
        } else if ("VARCHAR2".equals(dataType) || "CHAR".equals(dataType)) {
            long length = rs.getLong("DATA_LENGTH");
            return String.format("%s(%d)", dataType, length);
        }
        return dataType;
    }

}
