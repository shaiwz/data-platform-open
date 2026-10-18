package cn.dataplatform.open.flow.service.core.component.query;

import cn.dataplatform.open.common.component.LimitAdjuster;
import cn.dataplatform.open.common.component.MemoryLimitAdjuster;
import cn.dataplatform.open.common.source.JdbcSource;
import cn.dataplatform.open.common.source.SourceManager;
import cn.dataplatform.open.flow.service.core.Context;
import cn.dataplatform.open.flow.service.core.Flow;
import cn.dataplatform.open.flow.service.core.Transmit;
import cn.dataplatform.open.flow.service.core.component.FlowComponent;
import cn.dataplatform.open.flow.service.core.record.BatchPlainRecord;
import cn.dataplatform.open.flow.service.core.record.PlainRecord;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/7/13
 * @since 1.0.0
 */
@Slf4j
@Setter
@Getter
public abstract class JDBCQueryFlowComponent extends FlowComponent {

    private static final LimitAdjuster LIMIT_ADJUSTER = new MemoryLimitAdjuster();

    private final SourceManager sourceManager;

    @NotBlank
    private String datasourceCode;
    @NotBlank
    private String selectText;

    /**
     * 单位秒
     */
    private Integer queryTimeout = 6;

    /**
     * 查询一次的数据量
     * </p>
     * 不指定则动态
     */
    private Long limit;

    /**
     * 自动方式-通过游标,每次拉取数量根据内存压力自动调整
     */
    private boolean streamQuery = true;

    /**
     * 滚动查询列
     */
    private String scrollColumn;

    /**
     * 实际滚动列名称，与数据库字段大小写保持一致
     * <p>
     * 这里缓存使用
     */
    private String actualScrollColumn;

    public JDBCQueryFlowComponent(Flow flow, String code) {
        super(flow, code);
        this.sourceManager = this.getApplicationContext().getBean(SourceManager.class);
    }

    /**
     * 执行查询
     *
     * @param transmit 传输对象
     * @param context  上下文
     */
    @SneakyThrows
    @Override
    public void run(Transmit transmit, Context context) {
        log.info("开始执行查询:" + this.getCode() + ",查询语句:" + this.selectText);
        // 先根据内存压力获取每次拉取数量大小
        Long limit = this.limit;
        if (limit == null || limit <= 0) {
            limit = LIMIT_ADJUSTER.limit();
        }
        JdbcSource source = this.sourceManager.get(this.getWorkspaceCode(), this.getDatasourceCode(), JdbcSource.class);
        try (Connection connection = source.getConnection()) {
            if (this.streamQuery) {
                this.streamQuery(context, connection, limit);
            } else if (this.scrollColumn == null) {
                this.paginationQuery(context, connection, limit);
            } else {
                this.scrollQuery(context, connection, limit);
            }
        }
    }

    /**
     * 处理流式查询
     *
     * @param context    上下文
     * @param connection 数据库连接,流式查询会独占一个连接
     * @param limit      每次查询的数量
     * @throws SQLException SQL异常
     */
    public abstract void streamQuery(Context context, Connection connection, Long limit) throws Exception;


    /**
     * 处理分页查询
     *
     * @param context    上下文
     * @param connection 数据库连接
     * @param limit      每次查询的数量
     */
    public abstract void paginationQuery(Context context, Connection connection, Long limit) throws Exception;

    /**
     * 处理滚动查询
     *
     * @param context    上下文
     * @param connection 数据库连接
     * @param limit      每次查询的数量
     */
    public abstract void scrollQuery(Context context, Connection connection, Long limit) throws Exception;


    /**
     * 查询完成后的处理
     *
     * @param batchPlainRecord 查询结果
     * @param context          上下文
     */
    public void doNext(BatchPlainRecord batchPlainRecord, Context context) {
        if (batchPlainRecord.isEmpty()) {
            return;
        }
        log.info("开始传递数据到下一个节点");
        this.runNext(() -> {
            Transmit nextTransmit = new Transmit();
            nextTransmit.setFlowComponent(this);
            nextTransmit.setRecord(batchPlainRecord);
            return nextTransmit;
        }, context);
    }

    /**
     * 查询
     *
     * @param selectText 查询语句
     * @return 查询结果
     */
    @SuppressWarnings("all")
    @SneakyThrows
    public BatchPlainRecord query(Connection connection, String selectText) {
        log.info("开始查询:" + this.getKey() + ",查询语句:" + selectText);
        ResultSet resultSet = null;
        try (Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ) {
            statement.setQueryTimeout(this.getQueryTimeout());
            resultSet = statement.executeQuery(selectText);
            BatchPlainRecord records = new BatchPlainRecord();
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                Map<String, Object> record = new LinkedHashMap<>(metaData.getColumnCount());
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    value = this.convertResultValue(value);
                    record.put(columnName, value);
                }
                records.add(new PlainRecord(record));
            }
            log.info("查询完成:" + this.getCode() + ",查询到数据:" + records.size() + "条");
            return records;
        } finally {
            IoUtil.close(resultSet);
        }
    }

    /**
     * 转换查询结果值
     *
     * @param object 值
     * @return 转换后值
     */
    @SneakyThrows
    public Object convertResultValue(Object object) {
        if (object instanceof Clob clob) {
            return clob.getSubString(1, (int) clob.length());
        } else if (object instanceof Blob blob) {
            return blob.getBytes(1, (int) blob.length());
        }
        // 日期统一使用LocalDateTime
        else if (object instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        } else if (object instanceof java.util.Date date) {
            return new DateTime(date).toLocalDateTime();
        }
        return object;
    }


    /**
     * 获取滚动列的值
     *
     * @param map 数据
     * @return 值
     */
    public Object getScrollColumnValue(Map<String, Object> map) {
        if (CollUtil.isEmpty(map) || StrUtil.isBlank(this.scrollColumn)) {
            return null;
        }
        if (this.actualScrollColumn != null) {
            return map.get(this.actualScrollColumn);
        }
        // 先尝试精确匹配
        if (map.containsKey(this.scrollColumn)) {
            return map.get(this.scrollColumn);
        }
        // 忽略大小写匹配
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (this.scrollColumn.equalsIgnoreCase(entry.getKey())) {
                this.actualScrollColumn = entry.getKey();
                return entry.getValue();
            }
        }
        return null;
    }

}

