package cn.dataplatform.open.web.service.datasource.tables;


import cn.dataplatform.open.common.enums.DataSourceType;
import cn.dataplatform.open.common.util.IoUtils;
import cn.dataplatform.open.web.vo.data.source.SchemaTable;
import cn.dataplatform.open.web.vo.data.source.TableDetail;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

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
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve", "SqlDialectInspection"})
@Component
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
     * 获取此连接下所有的库
     *
     * @param connect 数据库连接
     * @return 库名列表
     */
    @SuppressWarnings("DuplicatedCode")
    @SneakyThrows
    @Override
    public List<String> schema(Connection connect) {
        List<String> excludedSchemas = this.excludedSchema();
        String collect = excludedSchemas.stream().map(s -> "?").collect(Collectors.joining(","));
        String sql = "SELECT DISTINCT TABLE_SCHEMA FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA NOT IN ("
                + collect + ") ORDER BY TABLE_SCHEMA";
        PreparedStatement ps = connect.prepareStatement(sql);
        ResultSet rs = null;
        try {
            for (int i = 0; i < excludedSchemas.size(); i++) {
                ps.setString(i + 1, excludedSchemas.get(i));
            }
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
        List<String> excludedSchemas = this.excludedSchema();
        String collect = excludedSchemas.stream().map(s -> "?").collect(Collectors.joining(","));
        StringBuilder sql = new StringBuilder("SELECT TABLE_SCHEMA  AS `schema`,\n" +
                "       TABLE_NAME    AS `table`,\n" +
                "       TABLE_COMMENT AS `comment`\n" +
                "FROM INFORMATION_SCHEMA.TABLES\n" +
                "WHERE TABLE_SCHEMA NOT IN (" + collect + ")");
        if (StrUtil.isNotBlank(schemaCode)) {
            sql.append(" AND TABLE_SCHEMA = ?");
        }
        if (StrUtil.isNotBlank(likeTable)) {
            sql.append(" AND TABLE_NAME LIKE ?");
        }
        sql.append(" ORDER BY TABLE_SCHEMA, TABLE_NAME");
        if (limit > 0) {
            sql.append(" LIMIT ?");
        }
        PreparedStatement preparedStatement = connect.prepareStatement(sql.toString());
        ResultSet resultSet = null;
        try {
            int idx = 1;
            for (String s : excludedSchemas) {
                preparedStatement.setString(idx++, s);
            }
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
        ResultSet tableInfoRs = null;
        ResultSet columnsRs = null;
        try {
            // 获取表的基本信息（创建时间、注释等）
            tableInfoStmt = connection.prepareStatement(
                    "SELECT TABLE_COMMENT, TABLE_ROWS, AVG_ROW_LENGTH, DATA_LENGTH, INDEX_LENGTH " +
                            "FROM INFORMATION_SCHEMA.TABLES " +
                            "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?");
            tableInfoStmt.setString(1, schema);
            tableInfoStmt.setString(2, table);
            tableInfoRs = tableInfoStmt.executeQuery();
            if (tableInfoRs.next()) {
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
        } finally {
            IoUtils.close(columnsRs, columnsStmt, tableInfoRs, tableInfoStmt);
        }
        return tableDetail;
    }

    /**
     * 获取数据源类型
     *
     * @return 数据源类型
     */
    @Override
    public DataSourceType getDataSourceType() {
        return DataSourceType.MYSQL;
    }

    /**
     * 获取排除的schema
     *
     * @return 排除的schema
     */
    @Override
    public List<String> excludedSchema() {
        return EXCLUDED_SCHEMA;
    }

}