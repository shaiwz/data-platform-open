package cn.dataplatform.open.flow.service.core.component.event.connector;

import cn.dataplatform.open.common.source.*;
import cn.dataplatform.open.flow.service.core.component.event.DebeziumFlowComponent;
import cn.dataplatform.open.common.source.MySQLDataSource;
import cn.dataplatform.open.common.source.PostgreSQLDataSource;
import io.debezium.config.Configuration;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/6/27
 * @since 1.0.0
 */
public interface ConnectorConfigure {


    /**
     * 配置Debezium连接器
     *
     * @param debeziumFlowComponent Debezium流组件
     * @param source                数据源
     * @param builder               配置构建器
     */
    void configure(DebeziumFlowComponent debeziumFlowComponent, Source source,
                   Configuration.Builder builder);


    /**
     * 获取连接器配置类
     *
     * @param source 数据源
     * @return ConnectorConfigure 实例
     */
    static ConnectorConfigure get(Source source) {
        switch (source) {
            case MySQLDataSource ignored -> {
                return new MySQLConnectorConfigure();
            }
            case PostgreSQLDataSource ignored -> {
                return new PostgreSQLConnectorConfigure();
            }
            default -> throw new UnsupportedOperationException("不支持的数据源类型: " + source.type());
        }
    }

}
