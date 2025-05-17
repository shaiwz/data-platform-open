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
 * DamengDataSource
 *
 * @author dqw
 * @since 1.0.0
 */
@Slf4j
@Data
public class DamengDataSource implements JDBCSource {

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

    private List<String> serverNodes;

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
                    log.info("初始化Dameng数据源:" + this.getUrl());
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(url); // 数据库连接URL
                    config.setUsername(username); // 数据库用户名
                    config.setPassword(password); // 数据库密码
                    config.setDriverClassName(driverClassName); // 数据库驱动类名
                    config.setMinimumIdle(5); // 最小空闲连接数
                    config.setMaximumPoolSize(this.maxPoolSize); // 最大连接数
                    config.setIdleTimeout(30000); // 空闲连接超时时间
                    config.setConnectionTimeout(30000); // 连接超时时间
                    config.setInitializationFailTimeout(-1); // 初始化失败超时时间，-1表示无限重试
                    dataSource = new HikariDataSource(config);
                    log.info("初始化Dameng数据源完成");
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
        return DataSourceType.DAMENG;
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
            return connection.createStatement().execute("SELECT 1 FROM DUAL");
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