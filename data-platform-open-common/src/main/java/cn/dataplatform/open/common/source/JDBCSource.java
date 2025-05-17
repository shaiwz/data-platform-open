package cn.dataplatform.open.common.source;

import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/5/17
 * @since 1.0.0
 */
public interface JDBCSource extends Source {

    /**
     * 获取连接
     *
     * @return 连接
     */
    default Connection getConnection() {
        return this.getConnection(true);
    }

    /**
     * 获取数据源
     *
     * @return DataSource
     */
    DataSource getDataSource();

    /**
     * 获取连接
     *
     * @param autoCommit 是否自动提交
     * @return 连接
     */
    Connection getConnection(boolean autoCommit);


    /**
     * 获取JdbcClient
     *
     * @return JdbcClient
     */
    JdbcClient getJdbcClient();

}
