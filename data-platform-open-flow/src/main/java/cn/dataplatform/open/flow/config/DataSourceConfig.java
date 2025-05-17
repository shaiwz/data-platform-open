package cn.dataplatform.open.flow.config;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.baomidou.dynamic.datasource.provider.AbstractDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.*;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/15
 * @since 1.0.0
 */
@Configuration
public class DataSourceConfig {

    @Resource
    private DynamicDataSourceProperties properties;
    @Resource
    private ShardingProperties shardingProperties;

    /**
     * 动态数据源提供者
     *
     * @param dataSourceCreator 数据源创建器
     * @return result
     */
    @Bean
    public DynamicDataSourceProvider dynamicDataSourceProvider(DefaultDataSourceCreator dataSourceCreator) {
        Map<String, DataSourceProperty> datasource = properties.getDatasource();
        return new AbstractDataSourceProvider(dataSourceCreator) {

            @SneakyThrows
            @Override
            public Map<String, DataSource> loadDataSources() {
                Map<String, DataSource> dataSourceMap = this.createDataSourceMap(datasource);
                Set<String> datasourceNames = dataSourceMap.keySet();
                for (String datasourceName : datasourceNames) {
                    DataSource dataSource = dataSourceMap.get(datasourceName);
                    ShardingProperties.Config config = shardingProperties.getDatasource().get(datasourceName);
                    if (config != null) {
                        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
                        // 分表配置
                        Map<String, ShardingProperties.Config.Table> tables = config.getTables();
                        for (Map.Entry<String, ShardingProperties.Config.Table> entry : tables.entrySet()) {
                            ShardingTableRuleConfiguration shardingTableRuleConfiguration = this.getShardingTableRuleConfiguration(entry);
                            shardingRuleConfiguration.getTables().add(shardingTableRuleConfiguration);
                        }
                        // 分表策略
                        Map<String, ShardingProperties.Config.ShardingAlgorithms> shardingAlgorithms = config.getShardingAlgorithms();
                        for (Map.Entry<String, ShardingProperties.Config.ShardingAlgorithms> entry : shardingAlgorithms.entrySet()) {
                            String algorithmName = entry.getKey();
                            ShardingProperties.Config.ShardingAlgorithms shardingAlgorithm = entry.getValue();
                            Map<String, String> map = shardingAlgorithm.getProps();
                            Properties props = new Properties();
                            props.putAll(map);
                            shardingRuleConfiguration.getShardingAlgorithms().put(algorithmName, new AlgorithmConfiguration(shardingAlgorithm.getType(), props));
                        }
                        Map<String, DataSource> dsm = new LinkedHashMap<>(Collections.singletonMap(datasourceName, ((ItemDataSource) dataSource).getDataSource()));
                        SingleRuleConfiguration singleRuleConfiguration = new SingleRuleConfiguration();
                        singleRuleConfiguration.setTables(List.of(datasourceName + ".*"));
                        // 其他配置
                        Properties prop = new Properties();
                        Map<String, Object> props = config.getProps();
                        prop.putAll(props);
                        DataSource newDataSource = ShardingSphereDataSourceFactory.createDataSource(dsm, List.of(shardingRuleConfiguration, singleRuleConfiguration), prop);
                        dataSourceMap.put(datasourceName, newDataSource);
                    }
                }
                return dataSourceMap;
            }

            /**
             * 分表配置
             *
             * @param entry entry
             * @return result
             */
            private static ShardingTableRuleConfiguration getShardingTableRuleConfiguration(Map.Entry<String, ShardingProperties.Config.Table> entry) {
                String tableName = entry.getKey();
                ShardingProperties.Config.Table table = entry.getValue();
                ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration(tableName, table.getActualDataNodes());
                ShardingProperties.Config.Table.TableStrategy tableStrategy = table.getTableStrategy();
                ShardingProperties.Config.Table.TableStrategy.Standard standard = tableStrategy.getStandard();
                ShardingStrategyConfiguration tableStrategyConfiguration = new StandardShardingStrategyConfiguration(standard.getShardingColumn(),
                        standard.getShardingAlgorithmName());
                shardingTableRuleConfiguration.setTableShardingStrategy(tableStrategyConfiguration);
                return shardingTableRuleConfiguration;
            }

        };
    }


}
