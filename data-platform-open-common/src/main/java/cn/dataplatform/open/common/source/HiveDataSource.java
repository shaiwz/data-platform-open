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

/**
 * Hive数据源实现
 *
 * @author [你的名字]
 * @since 1.0.0
 */
@Slf4j
@Data
public class HiveDataSource implements JDBCSource {

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

    private Integer maxPoolSize;

    /**
     * 数据源实例，使用volatile保证多线程可见性
     * 后面可以做超过指定时间自动销毁的功能
     */
    @Setter(AccessLevel.NONE)
    private volatile DataSource dataSource;
    @Setter(AccessLevel.NONE)
    private volatile JdbcClient jdbcClient;

    /**
     * 获取JdbcClient
     *
     * @return JdbcClient实例
     */
    @Override
    public JdbcClient getJdbcClient() {
        if (jdbcClient == null) {
            synchronized (this) {
                if (jdbcClient == null) {
                    log.info("初始化Hive JdbcClient: {}", this.url);
                    jdbcClient = JdbcClient.create(this.getDataSource());
                    log.info("初始化Hive JdbcClient完成");
                }
            }
        }
        return jdbcClient;
    }

    /**
     * 获取数据源
     *
     * @return DataSource实例
     */
    @Override
    public DataSource getDataSource() {
        // 双重检查锁定实现延迟初始化
        if (dataSource == null) {
            synchronized (this) {
                if (dataSource == null) {
                    log.info("初始化Hive数据源: {}", this.getUrl());
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(url);
                    config.setUsername(username);
                    config.setPassword(password);
                    // org.apache.hive.jdbc.HiveDriver
                    config.setDriverClassName(driverClassName);
                    config.setMinimumIdle(5);
                    config.setMaximumPoolSize(this.maxPoolSize != null ? this.maxPoolSize : 10);
                    config.setIdleTimeout(30000);         // 空闲连接超时时间(毫秒)
                    config.setConnectionTimeout(30000);   // 连接超时时间(毫秒)
                    config.setInitializationFailTimeout(-1); // 初始化失败超时时间，-1表示无限重试
                    dataSource = new HikariDataSource(config);
                    log.info("初始化Hive数据源完成");
                }
            }
        }
        return dataSource;
    }

    /**
     * 获取数据库连接
     *
     * @param autoCommit 是否自动提交事务
     * @return Connection实例
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
     * 获取数据源编码
     *
     * @return 数据源编码
     */
    @Override
    public String code() {
        return this.getCode();
    }

    /**
     * 获取数据源名称
     *
     * @return 数据源名称
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * 获取数据源类型
     *
     * @return 数据源类型枚举
     */
    @Override
    public DataSourceType type() {
        return DataSourceType.HIVE;
    }

    /**
     * 是否启用健康检查
     *
     * @return true表示启用健康检查
     */
    @Override
    public Boolean isEnableHealth() {
        return isEnableHealth;
    }

    /**
     * 健康检查
     *
     * @return true表示健康
     * @throws Exception 健康检查过程中可能出现的异常
     */
    @Override
    public Boolean health() throws Exception {
        Connection connection = null;
        try {
            // 加载Hive JDBC驱动
            Class.forName(driverClassName);
            // 尝试建立数据库连接
            connection = DriverManager.getConnection(url, username, password);
            // 如果连接成功且有效，说明数据库健康
            return connection.isValid(3000);
        } finally {
            // 确保关闭数据库连接
            IoUtil.close(connection);
        }
    }

    /**
     * 关闭数据源
     */
    @Override
    public void close() {
        if (this.dataSource != null) {
            if (this.dataSource instanceof AutoCloseable closeable) {
                IoUtil.close(closeable);
            }
            this.dataSource = null;
            this.jdbcClient = null;
            log.info("Hive数据源已关闭");
        }
    }

}