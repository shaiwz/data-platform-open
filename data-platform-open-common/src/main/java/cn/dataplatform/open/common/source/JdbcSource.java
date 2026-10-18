package cn.dataplatform.open.common.source;

import cn.dataplatform.open.common.exception.DataSourceException;
import cn.dataplatform.open.common.util.MDCUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import com.zaxxer.hikari.util.PropertyElf;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.yaml.YamlJDBCConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlModeConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Jdbc数据源
 *
 * @author dingqianwen
 * @date 2025/5/17
 * @since 1.0.0
 */
@Getter
@Setter
@Slf4j
public abstract class JdbcSource extends Source {

    private static final Field CONTEXT_MANAGER_FIELD = FieldUtils.getDeclaredField(ShardingSphereDataSource.class,
            "contextManager", true);
    private static final Field DATABASE_NAME_FIELD = FieldUtils.getDeclaredField(ShardingSphereDataSource.class,
            "databaseName", true);
    private ApplicationContext applicationContext;
    /**
     * 优雅关闭
     */
    private boolean gracefulClose = true;
    /**
     * 优雅关闭最大等待时间（单位：秒）
     * 默认5分钟
     */
    private int gracefulCloseMaxWaitTime = 300;

    @NotBlank
    protected String driverClassName;
    protected String connectionTestQuery;

    private String partitioningAlgorithm;
    /**
     * 数据源
     */
    @Setter(AccessLevel.NONE)
    protected volatile DataSource dataSource;
    @Setter(AccessLevel.NONE)
    protected volatile JdbcClient jdbcClient;
    @Setter(AccessLevel.NONE)
    protected volatile NamedParameterJdbcTemplate namedJdbcTemplate;
    @Setter(AccessLevel.NONE)
    protected volatile JdbcTemplate jdbcTemplate;
    @Setter(AccessLevel.NONE)
    protected volatile PlatformTransactionManager txManager;

    /**
     * 初始化数据源
     */
    @SneakyThrows
    @Override
    public void init() {
        super.init();
        this.applicationContext = SpringUtil.getApplicationContext();
        this.gracefulClose = this.getBooleanProperty(LambdaUtil.getFieldName(this::isGracefulClose), this.gracefulClose);
        this.gracefulCloseMaxWaitTime = this.getIntProperty(LambdaUtil.getFieldName(this::getGracefulCloseMaxWaitTime), this.gracefulCloseMaxWaitTime);
        // 加载当前数据源驱动
        Class.forName(this.driverClassName);
    }

    /**
     * 获取JdbcClient
     *
     * @return JdbcClient
     */
    public JdbcClient getJdbcClient() {
        if (this.jdbcClient == null) {
            synchronized (this) {
                if (this.jdbcClient == null) {
                    log.info("初始化 JdbcClient");
                    this.jdbcClient = JdbcClient.create(this.getDataSource());
                    log.info("初始化 JdbcClient  完成");
                }
            }
        }
        return this.jdbcClient;
    }

    /**
     * 获取NamedParameterJdbcTemplate
     *
     * @return NamedParameterJdbcTemplate
     */
    public NamedParameterJdbcTemplate getNamedJdbcTemplate() {
        if (this.namedJdbcTemplate == null) {
            synchronized (this) {
                if (this.namedJdbcTemplate == null) {
                    log.info("初始化 NamedParameterJdbcTemplate");
                    this.namedJdbcTemplate = new NamedParameterJdbcTemplate(this.getDataSource());
                    log.info("初始化 NamedParameterJdbcTemplate 完成");
                }
            }
        }
        return this.namedJdbcTemplate;
    }

    /**
     * 获取JdbcTemplate
     *
     * @return JdbcTemplate
     */
    public JdbcTemplate getJdbcTemplate() {
        if (this.jdbcTemplate == null) {
            synchronized (this) {
                if (this.jdbcTemplate == null) {
                    log.info("初始化 JdbcTemplate");
                    this.jdbcTemplate = new JdbcTemplate(this.getDataSource());
                    log.info("初始化 JdbcTemplate 完成");
                }
            }
        }
        return this.jdbcTemplate;
    }

    /**
     * 获取PlatformTransactionManager
     *
     * @return PlatformTransactionManager
     */
    public PlatformTransactionManager getTxManager() {
        if (this.txManager == null) {
            synchronized (this) {
                if (this.txManager == null) {
                    log.info("初始化 PlatformTransactionManager");
                    this.txManager = new DataSourceTransactionManager(this.getDataSource());
                    log.info("初始化 PlatformTransactionManager 完成");
                }
            }
        }
        return this.txManager;
    }

    /**
     * 获取数据源
     *
     * @return 数据源
     */
    public DataSource getDataSource() {
        if (this.dataSource == null) {
            synchronized (this) {
                if (this.dataSource == null) {
                    log.info("初始化 JDBC 数据源: {}({})", this.code, this.name);
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(this.url);
                    config.setUsername(this.username);
                    config.setPassword(this.password);
                    config.setDriverClassName(this.driverClassName);
                    // 初始化失败超时时间，小于0表示跳过所有初始化检查并立即启动池。
                    config.setInitializationFailTimeout(-1);
                    // 连接测试查询语句
                    String connectionTestQuery = this.getConnectionTestQuery();
                    if (StrUtil.isNotBlank(connectionTestQuery)) {
                        config.setConnectionTestQuery(connectionTestQuery);
                    }
                    // 自定义连接参数
                    // dataSource.允许设置数据源属性
                    Properties p = this.getProperties();
                    if (CollUtil.isNotEmpty(p)) {
                        log.info("设置自定义连接属性: {}", p);
                        PropertyElf.setTargetFromProperties(config, p);
                    }
                    HikariDataSource hikariDataSource = new HikariDataSource(config);
                    // validate之后打印正确的
                    Map<String, Object> configMap = BeanUtil.beanToMap(config);
                    // 移除password等敏感信息
                    configMap.remove(LambdaUtil.getFieldName(HikariConfig::getPassword));
                    log.info("JDBC 数据源配置参数: {}", JSON.toJSONString(configMap, JSONWriter.Feature.PrettyFormat));
                    this.dataSource = this.createShardingDataSource(this.partitioningAlgorithm, hikariDataSource);
                    log.info("初始化 JDBC 数据源完成");
                }
            }
        }
        return this.dataSource;
    }

    /**
     * 获取连接
     *
     * @return 连接
     */
    public Connection getConnection() {
        return this.getConnection(true);
    }

    /**
     * 获取连接
     *
     * @param autoCommit 是否自动提交
     * @return 连接
     */
    @SneakyThrows
    public Connection getConnection(boolean autoCommit) {
        // 如果数据源为空则初始化
        DataSource dataSource = this.getDataSource();
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    /**
     * 创建分片数据源
     *
     * @param partitioningAlgorithm 分表规则
     * @param hikariDataSource      原始数据源
     * @return 分片数据源
     */
    public DataSource createShardingDataSource(String partitioningAlgorithm, HikariDataSource hikariDataSource) {
        if (StrUtil.isBlank(partitioningAlgorithm)) {
            // 普通数据源
            return hikariDataSource;
        }
        // @see https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/
        log.info("初始化分表规则: {}", partitioningAlgorithm);
        try {
            YamlJDBCConfiguration jdbcConfig = YamlEngine.unmarshal(partitioningAlgorithm, YamlJDBCConfiguration.class);
            String databaseName = jdbcConfig.getDatabaseName();
            Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(1);
            dataSourceMap.put(Objects.requireNonNullElse(databaseName, DefaultDatabase.LOGIC_NAME), hikariDataSource);
            return this.createDataSource(dataSourceMap, jdbcConfig);
        } catch (Exception e) {
            throw new DataSourceException("初始化分表规则失败", e);
        }
    }

    /**
     * 创建Sharding数据源代理
     *
     * @param dataSourceMap 数据源
     * @param jdbcConfig    配置
     * @return 数据源
     */
    public DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final YamlJDBCConfiguration jdbcConfig)
            throws SQLException {
        ModeConfiguration modeConfig = null == jdbcConfig.getMode() ? null :
                new YamlModeConfigurationSwapper().swapToObject(jdbcConfig.getMode());
        jdbcConfig.rebuild();
        Collection<RuleConfiguration> ruleConfigs = new YamlRuleConfigurationSwapperEngine().
                swapToRuleConfigurations(jdbcConfig.getRules());
        return ShardingSphereDataSourceFactory.createDataSource(jdbcConfig.getDatabaseName(),
                modeConfig, dataSourceMap, ruleConfigs, jdbcConfig.getProps());
    }

    /**
     * 健康检查，失败抛出异常
     *
     * @throws Exception 检查失败原因
     */
    @SuppressWarnings({"SqlSourceToSinkFlow"})
    @Override
    public void health() throws Exception {
        // 加载数据库驱动由 init 完成；此处直连校验连通性
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             var statement = connection.createStatement()) {
            statement.execute(this.getConnectionTestQuery());
        }
    }

    /**
     * 关闭数据源
     */
    @SneakyThrows
    @Override
    public synchronized void close() {
        log.info("关闭 JDBC 数据源: {}({})", this.code, this.name);
        if (this.dataSource != null) {
            boolean active = true;
            if (this.applicationContext instanceof ConfigurableApplicationContext configurableContext) {
                active = configurableContext.isActive();
            }
            if (this.gracefulClose &&
                    // 并且应用程序没有关闭，否则直接强制关闭数据源即可
                    Objects.equals(active, true)) {
                // 防止异步操作时，下面被this.dataSource = null;
                DataSource cds = this.dataSource;
                // 虚拟线程异步关闭
                String requestId = MDCUtils.getRequestId();
                Thread.ofVirtual().start(() -> {
                    MDCUtils.setRequestId(requestId);
                    try {
                        try {
                            log.info("开始优雅关闭数据源, 最大等待时间: {}秒", this.gracefulCloseMaxWaitTime);
                            long startTime = System.currentTimeMillis();
                            long maxWaitMillis = this.gracefulCloseMaxWaitTime * 1000L;
                            int totalActiveConnections;
                            do {
                                totalActiveConnections = this.getActiveConnectionCount(cds);
                                if (totalActiveConnections == 0) {
                                    log.info("当前活跃连接数为0, 开始关闭数据源");
                                    break;
                                }
                                long elapsed = System.currentTimeMillis() - startTime;
                                if (elapsed >= maxWaitMillis) {
                                    log.warn("等待超时, 当前仍有 {} 个活跃连接, 强制关闭数据源", totalActiveConnections);
                                    break;
                                }
                                log.info("当前活跃连接数: {}, 已等待 {} 秒, 继续等待...",
                                        totalActiveConnections, elapsed / 1000);
                                // 每秒检查一次
                                ThreadUtil.sleep(500);
                            } while (true);
                            // 执行关闭
                        } catch (Exception e) {
                            log.error("优雅关闭数据源失败, 开始强制关闭", e);
                        }
                        // 关闭数据源
                        if (cds instanceof AutoCloseable closeable) {
                            IoUtil.close(closeable);
                        }
                    } finally {
                        MDC.clear();
                    }
                });
            } else {
                // 直接关闭
                if (this.dataSource instanceof AutoCloseable closeable) {
                    IoUtil.close(closeable);
                }
            }
            this.namedJdbcTemplate = null;
            this.jdbcTemplate = null;
            this.txManager = null;
            this.dataSource = null;
            this.jdbcClient = null;
        }
    }

    /**
     * 获取当前活跃连接数
     *
     * @return 活跃连接数
     */
    @SneakyThrows
    private int getActiveConnectionCount(DataSource dataSource) {
        if (dataSource instanceof ShardingSphereDataSource sphereDataSource) {
            String databaseName = (String) DATABASE_NAME_FIELD.get(sphereDataSource);
            ContextManager contextManager = (ContextManager) CONTEXT_MANAGER_FIELD.get(sphereDataSource);
            Map<String, StorageUnit> storageUnits = contextManager.getStorageUnits(databaseName);
            return this.getTotalActiveConnections(storageUnits);
        } else if (dataSource instanceof HikariDataSource hikariDataSource) {
            HikariPoolMXBean hikariPoolMXBean = hikariDataSource.getHikariPoolMXBean();
            if (hikariPoolMXBean != null) {
                return hikariPoolMXBean.getActiveConnections();
            }
            return 0;
        } else {
            log.warn("无法获取活跃连接数, 数据源类型不受支持: {}", dataSource.getClass().getName());
            return 0;
        }
    }

    /**
     * 获取所有存储单元中的活跃连接总数
     *
     * @param storageUnits 存储单元映射，key为存储单元名称，value为存储单元对象
     * @return 所有存储单元中HikariDataSource数据源的活跃连接总数
     */
    private int getTotalActiveConnections(Map<String, StorageUnit> storageUnits) {
        Collection<StorageUnit> values = storageUnits.values();
        int totalActiveConnections = 0;
        for (StorageUnit value : values) {
            DataSource ds = value.getDataSource();
            if (ds instanceof HikariDataSource hikariDataSource) {
                HikariPoolMXBean hikariPoolMXBean = hikariDataSource.getHikariPoolMXBean();
                if (hikariPoolMXBean != null) {
                    totalActiveConnections += hikariPoolMXBean.getActiveConnections();
                }
            }
        }
        return totalActiveConnections;
    }

}
