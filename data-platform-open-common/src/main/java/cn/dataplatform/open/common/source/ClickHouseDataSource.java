package cn.dataplatform.open.common.source;

import cn.dataplatform.open.common.enums.DataSourceType;
import cn.hutool.core.io.IoUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * ClickHouseDataSource
 *
 * @author dqw
 * @since 1.0.0
 */
@Slf4j
@Data
public class ClickHouseDataSource implements JDBCSource {

    private String code;
    private String name;

    /**
     * 连接信息
     */
    @NotBlank
    private String url;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String driverClassName;
    private Boolean isEnableHealth;

    /**
     * ClickHouse集群节点
     */
    private List<String> nodes;

    private Integer maxPoolSize;

    /**
     * 后面做超过半小时或者指定时间自动销毁
     */
    @Setter(AccessLevel.NONE)
    private volatile DataSource dataSource;
    @Setter(AccessLevel.NONE)
    private volatile JdbcClient jdbcClient;

    /**
     * 获取JdbcClient
     *
     * @return JdbcClient
     */
    @Override
    public JdbcClient getJdbcClient() {
        if (jdbcClient == null) {
            synchronized (this) {
                if (jdbcClient == null) {
                    log.info("初始化JdbcClient:" + this.url);
                    jdbcClient = JdbcClient.create(this.getDataSource());
                    log.info("初始化JdbcClient完成");
                }
            }
        }
        return jdbcClient;
    }

    /**
     * 获取数据源
     *
     * @return DataSource
     */
    @Override
    public DataSource getDataSource() {
        // 如果数据源为空则初始化
        if (dataSource == null) {
            synchronized (this) {
                if (dataSource == null) {
                    log.info("初始化ClickHouse数据源:" + this.getUrl());
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(url);
                    config.setUsername(username);
                    config.setPassword(password);
                    config.setDriverClassName(driverClassName);
                    config.setMinimumIdle(5);
                    config.setMaximumPoolSize(this.maxPoolSize != null ? this.maxPoolSize : 10);
                    config.setIdleTimeout(30000);
                    config.setConnectionTimeout(30000);
                    config.setInitializationFailTimeout(-1);
                    dataSource = new HikariDataSource(config);
                    log.info("初始化ClickHouse数据源完成");
                }
            }
        }
        return dataSource;
    }

    /**
     * 获取连接
     *
     * @param autoCommit 是否自动提交
     * @return Connection
     */
    @Override
    @SneakyThrows
    public Connection getConnection(boolean autoCommit) {
        DataSource dataSource = this.getDataSource();
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    /**
     * 获取连接
     */
    @Override
    public String code() {
        return this.getCode();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public DataSourceType type() {
        return DataSourceType.CLICKHOUSE;
    }

    /**
     * 是否启用健康检查
     *
     * @return true启用
     */
    @Override
    public Boolean isEnableHealth() {
        return isEnableHealth;
    }

    /**
     * 健康检查
     *
     * @return true健康
     */
    @Override
    public Boolean health() throws Exception {
        Connection connection = null;
        try {
            // 加载数据库驱动
            Class.forName(driverClassName);
            // 尝试建立数据库连接
            connection = DriverManager.getConnection(url, username, password);
            // 执行简单查询验证连接
            return connection.createStatement().execute("SELECT 1");
        } finally {
            // 关闭数据库连接
            IoUtil.close(connection);
        }
    }

    /**
     * 关闭数据源
     */
    @Override
    public void close() {
        if (dataSource != null) {
            if (this.dataSource instanceof AutoCloseable closeable) {
                IoUtil.close(closeable);
            }
            dataSource = null;
            this.jdbcClient = null;
        }
    }

}