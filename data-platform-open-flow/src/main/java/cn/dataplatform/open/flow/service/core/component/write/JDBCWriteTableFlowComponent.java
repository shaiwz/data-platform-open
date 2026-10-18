package cn.dataplatform.open.flow.service.core.component.write;

import cn.dataplatform.open.common.source.JdbcSource;
import cn.dataplatform.open.common.source.SourceManager;
import cn.dataplatform.open.common.util.MapUtils;
import cn.dataplatform.open.flow.service.core.Context;
import cn.dataplatform.open.flow.service.core.Flow;
import cn.dataplatform.open.flow.service.core.Transmit;
import cn.dataplatform.open.flow.service.core.component.FlowComponent;
import cn.dataplatform.open.flow.service.core.pack.Column;
import cn.dataplatform.open.flow.service.core.record.Record;
import cn.dataplatform.open.flow.service.core.record.*;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/5/28
 * @since 1.0.0
 */
@Slf4j
@Getter
@Setter
public abstract class JDBCWriteTableFlowComponent extends FlowComponent {

    public static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    public static final String COLUMN_NAME = "COLUMN_NAME";
    public static final String DATA_TYPE = "DATA_TYPE";
    public static final String TYPE_NAME = "TYPE_NAME";

    /**
     * 存储表列结构
     */
    private static final Cache<String, List<Column>> TABLE_SCHEMA_CACHE = CacheUtil.newLRUCache(1000,
            60 * 1000);


    private SourceManager sourceManager;

    /**
     * 数据源编码
     */
    protected String datasourceCode;
    /**
     * 写入的数据库
     */
    protected String schema;
    /**
     * 写入的表，MySQL等支持分表（数据源编辑处配置）
     */
    protected String table;
    /**
     * 需要支持自定义SQL
     * <p>
     * DELETE : delete from table where id = ${id}
     * INSERT : insert into table (id,name) values (${id},${name})
     * UPDATE : update table set name = ${name} where id = ${id}
     */
    protected Map<Record.Op, CustomSQL> customSQL = new LinkedHashMap<>(3);

    /**
     * 主键列,主要用来做自动更新和删除
     * <p>
     * 后面是否修改为自动获取？
     */
    @NotNull
    protected List<String> primaryKey;

    /**
     * 批量写入大小
     */
    protected int batchSize = 1000;

    /**
     * 匹配数据时是否忽略大小写
     * <p>
     * 默认不区分大小写
     */
    protected boolean caseInsensitive = true;

    /**
     * 构造方法
     *
     * @param flow 流程
     * @param code 当前组件
     */
    public JDBCWriteTableFlowComponent(Flow flow, String code) {
        super(flow, code);
        this.sourceManager = this.getApplicationContext().getBean(SourceManager.class);
    }

    /**
     * 写入数据
     *
     * @param transmit 传输对象
     * @param context  上下文
     */
    public void run(Transmit transmit, Context context) {
        Record transmitRecord = transmit.getRecord();
        if (transmitRecord.isEmpty()) {
            return;
        }
        log.info("写入数据:" + this.getCode() + ",数据数量:" + transmitRecord.size());
        // 获取数据源
        JdbcSource jdbcSource = this.sourceManager.get(this.getWorkspaceCode(), this.getDatasourceCode(), JdbcSource.class);
        Connection connection = jdbcSource.getConnection(false);
        try {
            // 查询表结构
            List<Column> tableColumns = this.getTableColumns(connection, this.datasourceCode, this.schema, this.table);
            if (CollUtil.isEmpty(tableColumns)) {
                throw new RuntimeException("表不存在或表结构不存在:" + this.datasourceCode + "." + this.schema + "." + this.table);
            }
            Record.Op op = Record.Op.INSERT;
            Record record = transmit.getRecord();
            List<Map<String, Object>> rows;
            switch (record) {
                case BatchPlainRecord batchPlainRecord ->
                        rows = batchPlainRecord.getRecords().stream().map(PlainRecord::getRow).collect(Collectors.toList());
                case BatchStreamRecord batchStreamRecord -> {
                    List<StreamRecord> records = batchStreamRecord.getRecords();
                    rows = new ArrayList<>();
                    Record.Op operation = null;
                    for (StreamRecord streamRecord : records) {
                        if (operation != null && operation != streamRecord.getOperation()) {
                            // 类型不一样了 执行一个批次
                            // 不能聚合执行,因为时间线不一样
                            this.doWriteBefore(op, tableColumns, connection, rows);
                            rows.clear();
                        }
                        op = streamRecord.getOperation();
                        // 组装SQL
                        if (Objects.equals(streamRecord.getOperation(), Record.Op.DELETE)) {
                            rows.add(streamRecord.getBefore());
                        } else if (Objects.equals(streamRecord.getOperation(), Record.Op.UPDATE)) {
                            rows.add(streamRecord.getAfter());
                        } else {
                            rows.add(streamRecord.getAfter());
                        }
                        operation = streamRecord.getOperation();
                    }
                }
                case PlainRecord plainRecord -> rows = Collections.singletonList(plainRecord.getRow());
                case StreamRecord streamRecord -> {
                    op = streamRecord.getOperation();
                    if (Objects.equals(streamRecord.getOperation(), Record.Op.DELETE)) {
                        rows = Collections.singletonList(streamRecord.getBefore());
                    } else if (Objects.equals(streamRecord.getOperation(), Record.Op.UPDATE)) {
                        rows = Collections.singletonList(streamRecord.getAfter());
                    } else {
                        rows = Collections.singletonList(streamRecord.getAfter());
                    }
                }
                default -> throw new UnsupportedOperationException("不支持的数据类型:" + record.getClass());
            }
            this.doWriteBefore(op, tableColumns, connection, rows);
            log.info("写入数据成功:" + this.getCode() + ",数据数量:" + rows.size());
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                log.error("回滚事务失败", ex);
            }
            throw new RuntimeException("写入数据失败:" + this.getCode() + ",数据数量:" + transmit.getRecord().size(), e);
        } finally {
            IoUtil.close(connection);
        }
        // 允许继续执行下游节点，串行写入场景，或者当数据写入表成功后执行数据发送
        log.info("开始传递数据到下一个节点");
        this.runNext(() -> {
            Transmit nextTransmit = new Transmit();
            nextTransmit.setFlowComponent(this);
            nextTransmit.setRecord(transmitRecord);
            return nextTransmit;
        }, context);
    }

    /**
     * 分批写入数据
     *
     * @param op           写入类型
     * @param tableColumns 需要插入的列
     * @param connection   连接
     * @param rows         数据
     * @throws SQLException 异常
     */
    public void doWriteBefore(Record.Op op, List<Column> tableColumns, Connection connection,
                              List<Map<String, Object>> rows) throws SQLException {
        if (CollUtil.isEmpty(rows)) {
            return;
        }
        // 数据匹配忽略大小写
        // 作用域只限定与数据写入这块
        if (this.caseInsensitive) {
            rows = MapUtils.toCaseInsensitiveMap(rows);
        }
        // 如果数据量小于等于批量大小,直接执行
        if (rows.size() <= this.batchSize) {
            this.doWrite(op, tableColumns, connection, rows);
            return;
        }
        List<List<Map<String, Object>>> split = ListUtil.split(rows, this.batchSize);
        // 分批执行
        for (List<Map<String, Object>> batchRows : split) {
            this.doWrite(op, tableColumns, connection, batchRows);
        }
    }

    /**
     * 执行写入
     *
     * @param op           写入类型
     * @param tableColumns 需要插入的列
     * @param connection   连接
     * @param rows         数据
     * @throws SQLException 异常
     */
    public abstract void doWrite(Record.Op op, List<Column> tableColumns, Connection connection,
                                 List<Map<String, Object>> rows) throws SQLException;


    /**
     * 获取表列信息
     *
     * @param connection     连接
     * @param datasourceCode 数据源编码
     * @param schema         数据库名
     * @param table          表名
     * @return 列名列表
     */
    @SneakyThrows
    public List<Column> getTableColumns(Connection connection, String datasourceCode,
                                        String schema, String table) {
        String cacheKey = datasourceCode + "." + schema + "." + table;
        // 先从缓存获取
        List<Column> columns = TABLE_SCHEMA_CACHE.get(cacheKey);
        if (columns != null) {
            return columns;
        }
        // 经过实际测试,使用查询表结构的方式获取列信息更快,达到几倍性能
        // 这里优先使用查询表结构的方式获取
        @SuppressWarnings("all")
        String query = "SELECT * FROM " + schema + "." + table + " WHERE 1=0";
        try (Statement statement = connection.createStatement();
             @SuppressWarnings("all")
             ResultSet resultSet = statement.executeQuery(query)) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            columns = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                int columnType = metaData.getColumnType(i);
                String columnTypeName = metaData.getColumnTypeName(i);
                columns.add(new Column(columnName, columnType, columnTypeName));
            }
        } catch (Exception e) {
            log.warn("通过查询表结构获取列信息失败,尝试使用DatabaseMetaData获取", e);
            DatabaseMetaData metaData = connection.getMetaData();
            // bug修复查询指定catalog下的表结构
            // 例如a库有表admin b库也有表admin时读取重复问题
            try (ResultSet resultSet = metaData.getColumns(schema, schema, table, null)) {
                columns = new ArrayList<>();
                while (resultSet.next()) {
                    String columnName = resultSet.getString(COLUMN_NAME);
                    int columnType = resultSet.getInt(DATA_TYPE);
                    String columnTypeName = resultSet.getString(TYPE_NAME);
                    columns.add(new Column(columnName, columnType, columnTypeName));
                }
            }
        }
        TABLE_SCHEMA_CACHE.put(cacheKey, columns);
        return columns;
    }

    /**
     * 自定义SQL
     */
    @AllArgsConstructor
    @Data
    public static class CustomSQL {
        /**
         * 原始SQL
         * <p>
         * insert into table (id,name) values (${id},${name})
         */
        private String originScript;
        /**
         * 转换后SQL
         * <p>
         * insert into table (id,name) values (?,?)
         */
        private String script;
        /**
         * 变量名称
         */
        private List<String> variables;

        public CustomSQL(String script) {
            this.script = script;
            if (StrUtil.isBlank(script)) {
                return;
            }
            // 存在自定义SQL
            StringBuilder customSqlBuilder = new StringBuilder();
            Matcher matcher = VARIABLE_PATTERN.matcher(script);
            // 创建一个 List 来存储匹配到的变量名
            this.variables = new ArrayList<>();
            // 找到所有的匹配项并进行替换
            while (matcher.find()) {
                String variable = matcher.group(1);
                this.variables.add(variable);
                matcher.appendReplacement(customSqlBuilder, "?");
            }
            // 将剩余的部分添加到 StringBuffer 中
            matcher.appendTail(customSqlBuilder);
            this.script = customSqlBuilder.toString();
        }
    }

}