package cn.dataplatform.open.common.source;

import cn.dataplatform.open.common.enums.DataSourceType;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.yaml.YamlJDBCConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlModeConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author DaoDao
 */
@Slf4j
@Data
public class PostgreSQLDataSource implements JDBCSource {

    private String name;
    private String code;

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

    private String partitioningAlgorithm;
    private Integer maxPoolSize;

    private Boolean isEnableHealth;

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
     * @return 数据源
     */
    @Override
    public DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (this) {
                if (dataSource == null) {
                    log.info("初始化PostgreSQL数据源:" + this.url);
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
                    HikariDataSource hikariDataSource = new HikariDataSource(config);
                    log.info("初始化PostgreSQL数据源完成");
                    if (StrUtil.isNotBlank(partitioningAlgorithm)) {
                        // @see https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/
                        log.info("初始化分表规则:" + partitioningAlgorithm);
                        try {
                            YamlJDBCConfiguration jdbcConfig = YamlEngine.unmarshal(partitioningAlgorithm, YamlJDBCConfiguration.class);
                            String databaseName = jdbcConfig.getDatabaseName();
                            Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
                            dataSourceMap.put(Objects.requireNonNullElse(databaseName, DefaultDatabase.LOGIC_NAME), hikariDataSource);
                            dataSource = this.createDataSource(dataSourceMap, jdbcConfig);
                        } catch (Exception e) {
                            throw new RuntimeException("初始化分表规则失败", e);
                        }
                    } else {
                        // 普通数据源
                        dataSource = hikariDataSource;
                    }
                }
            }
        }
        return dataSource;
    }

    /**
     * 创建Sharding数据源代理
     *
     * @param dataSourceMap 数据源
     * @param jdbcConfig    配置
     * @return 数据源
     */
    private DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final YamlJDBCConfiguration jdbcConfig) throws SQLException {
        ModeConfiguration modeConfig = null == jdbcConfig.getMode() ? null : new YamlModeConfigurationSwapper().swapToObject(jdbcConfig.getMode());
        jdbcConfig.rebuild();
        Collection<RuleConfiguration> ruleConfigs = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(jdbcConfig.getRules());
        return ShardingSphereDataSourceFactory.createDataSource(jdbcConfig.getDatabaseName(), modeConfig, dataSourceMap, ruleConfigs, jdbcConfig.getProps());
    }

    /**
     * 获取连接
     *
     * @param autoCommit 是否自动提交
     * @return 连接
     */
    @Override
    @SneakyThrows
    public Connection getConnection(boolean autoCommit) {
        // 如果数据源为空则初始化
        DataSource dataSource = this.getDataSource();
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public DataSourceType type() {
        return DataSourceType.MYSQL;
    }

    @Override
    public Boolean isEnableHealth() {
        return this.isEnableHealth;
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
            // 如果连接成功,说明数据库健康
            return connection.isValid(3000);
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
        if (this.dataSource != null) {
            if (this.dataSource instanceof AutoCloseable closeable) {
                IoUtil.close(closeable);
            }
            this.dataSource = null;
            this.jdbcClient = null;
        }
    }

}