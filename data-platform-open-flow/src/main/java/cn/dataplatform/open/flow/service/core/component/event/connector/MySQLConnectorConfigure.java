package cn.dataplatform.open.flow.service.core.component.event.connector;

import cn.dataplatform.open.common.source.MySQLDataSource;
import cn.dataplatform.open.common.source.Source;
import cn.dataplatform.open.common.util.JdbcUtils;
import cn.dataplatform.open.flow.service.core.component.event.DebeziumFlowComponent;
import cn.hutool.core.util.RandomUtil;
import io.debezium.config.Configuration;
import io.debezium.connector.mysql.MySqlConnector;
import lombok.extern.slf4j.Slf4j;

/**
 * MySQL需要具备如下权限
 * GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'dp_test'@'%';
 * FLUSH PRIVILEGES;
 * SHOW GRANTS FOR 'dp_test'@'%';
 *
 * @author dingqianwen
 * @date 2025/6/27
 * @since 1.0.0
 */
@Slf4j
public class MySQLConnectorConfigure implements ConnectorConfigure {

    /**
     * 配置Debezium连接器
     *
     * @param debeziumFlowComponent Debezium流组件
     * @param source                数据源
     * @param builder               配置构建器
     */
    @Override
    public void configure(DebeziumFlowComponent debeziumFlowComponent, Source source, Configuration.Builder builder) {
        // 连接器的Java类名称
        builder.with("connector.class", MySqlConnector.class.getName());
        MySQLDataSource mySQLDataSource = (MySQLDataSource) source;
        // jdbc:mysql://**:3306/data_platform
        // 数据库的hostname
        builder.with("database.hostname", JdbcUtils.getHostname(mySQLDataSource.type(), mySQLDataSource.getUrl()));
        // 端口
        builder.with("database.port", JdbcUtils.getPort(mySQLDataSource.type(), mySQLDataSource.getUrl()));
        // 用户名
        builder.with("database.user", mySQLDataSource.getUsername());
        // 密码
        builder.with("database.password", mySQLDataSource.getPassword());
        // 包含的数据库列表,你的数据库
        builder.with("database.include.list", debeziumFlowComponent.getSchemas());
        // 推荐自己设置唯一的server.id
        if (debeziumFlowComponent.getDatabaseServerId() != null) {
            builder.with("database.server.id", debeziumFlowComponent.getDatabaseServerId());
        } else {
            // 12000-15000随机数
            // 修复随机id冲突引起的报错，改为110000000-120000000
            builder.with("database.server.id", RandomUtil.randomInt(110000000, 120000000));
        }
    }

}
