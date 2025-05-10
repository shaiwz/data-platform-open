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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/15
 * @since 1.0.0
 */
public class MySQLDataSourceTable implements DataSourceTable {

    private static final String SCHEMA = "schema";
    private static final String TABLE = "table";
    private static final String COMMENT = "comment";

    /**
     * 后面读取字典
     */
    public static final List<String> EXCLUDED_SCHEMA = CollUtil.newArrayList("mysql",
            "information_schema",
            "performance_schema", "sys");


    /**
     * 获取数据源下所有的库表
     * <p>
     * SELECT TABLE_SCHEMA  AS `schema`,
     * TABLE_NAME    AS `table`,
     * TABLE_COMMENT AS `comment`
     * FROM INFORMATION_SCHEMA.TABLES
     * WHERE TABLE_SCHEMA NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys');
     *
     * @return r
     */
    @SuppressWarnings("all")
    @SneakyThrows
    @Override
    public List<SchemaTable> schemaTable(Connection connect) {
        // 查询表 不能存在SQL注入问题 NOT IN使用占位符方式
        String collect = EXCLUDED_SCHEMA.stream().map(s -> "?").collect(Collectors.joining(","));
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT TABLE_SCHEMA  AS `schema`,\n" +
                "       TABLE_NAME    AS `table`,\n" +
                "       TABLE_COMMENT AS `comment`\n" +
                "FROM INFORMATION_SCHEMA.TABLES\n" +
                "WHERE TABLE_SCHEMA NOT IN (" + collect + ")");
        List<SchemaTable> arrayList;
        ResultSet resultSet = null;
        try {
            // 执行查询
            for (int i = 0; i < EXCLUDED_SCHEMA.size(); i++) {
                preparedStatement.setString(i + 1, EXCLUDED_SCHEMA.get(i));
            }
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
    @SneakyThrows
    @Override
    public TableDetail tableDetail(Connection connection, String schema, String table) {
        TableDetail tableDetail = new TableDetail();
        PreparedStatement tableInfoStmt = null;
        PreparedStatement columnsStmt = null;
        ResultSet tableInfoRs = null;
        ResultSet columnsRs = null;
        try {
            // 获取表的基本信息（创建时间、注释等）
            tableInfoStmt = connection.prepareStatement(
                    "SELECT CREATE_TIME, TABLE_COMMENT, TABLE_ROWS, AVG_ROW_LENGTH, DATA_LENGTH, INDEX_LENGTH " +
                            "FROM INFORMATION_SCHEMA.TABLES " +
                            "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?");
            tableInfoStmt.setString(1, schema);
            tableInfoStmt.setString(2, table);
            tableInfoRs = tableInfoStmt.executeQuery();
            if (tableInfoRs.next()) {
                Timestamp createTime = tableInfoRs.getTimestamp("CREATE_TIME");
                if (createTime != null) {
                    tableDetail.setCreateTime(createTime.toLocalDateTime());
                }
                tableDetail.setComment(tableInfoRs.getString("TABLE_COMMENT"));
            }
            // 获取表的列信息
            columnsStmt = connection.prepareStatement(
                    "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_COMMENT, " +
                            "       CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, COLUMN_DEFAULT " +
                            "FROM INFORMATION_SCHEMA.COLUMNS " +
                            "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                            "ORDER BY ORDINAL_POSITION");
            columnsStmt.setString(1, schema);
            columnsStmt.setString(2, table);
            columnsRs = columnsStmt.executeQuery();
            List<TableDetail.Column> columns = new ArrayList<>();
            while (columnsRs.next()) {
                TableDetail.Column column = new TableDetail.Column();
                column.setName(columnsRs.getString("COLUMN_NAME"));
                column.setType(columnsRs.getString("COLUMN_TYPE"));
                column.setComment(columnsRs.getString("COLUMN_COMMENT"));
                column.setNotNull("NO".equals(columnsRs.getString("IS_NULLABLE")));
                column.setPrimaryKey("PRI".equals(columnsRs.getString("COLUMN_KEY")));
                column.setDefaultValue(columnsRs.getString("COLUMN_DEFAULT"));
                // 设置长度和精度信息
                column.setMaxLength(columnsRs.getLong("CHARACTER_MAXIMUM_LENGTH"));
                columns.add(column);
            }
            tableDetail.setColumns(columns);
            // 3. 获取表的索引信息（可选）
            PreparedStatement indexesStmt = null;
            ResultSet indexesRs = null;
            try {
                indexesStmt = connection.prepareStatement(
                        "SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE " +
                                "FROM INFORMATION_SCHEMA.STATISTICS " +
                                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                                "ORDER BY INDEX_NAME, SEQ_IN_INDEX");
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
                            newIndex.setUnique(finalIndexesRs.getInt("NON_UNIQUE") == 0);
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
            }
        } finally {
            IoUtil.close(columnsRs);
            IoUtil.close(columnsStmt);
            IoUtil.close(tableInfoRs);
            IoUtil.close(tableInfoStmt);
        }
        return tableDetail;
    }

}
