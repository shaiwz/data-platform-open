package cn.dataplatform.open.flow.service.datasource.impl;

import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.enums.DataSourceType;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.exception.DataSourceException;
import cn.dataplatform.open.common.source.*;
import cn.dataplatform.open.common.util.IdUtils;
import cn.dataplatform.open.common.util.MDCUtils;
import cn.dataplatform.open.flow.service.DataSourceService;
import cn.dataplatform.open.flow.service.PasswordEncAndDecService;
import cn.dataplatform.open.flow.service.core.Flow;
import cn.dataplatform.open.flow.service.core.FlowEngine;
import cn.dataplatform.open.flow.store.entity.DataSource;
import cn.dataplatform.open.flow.store.mapper.DataSourceMapper;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;

/**
 * 数据源服务实现类
 *
 * @author dingqianwen
 * @date 2025/1/11
 * @since 1.0.0
 */
@Order(1)
@Slf4j
@Service
public class DataSourceServiceImpl extends ServiceImpl<DataSourceMapper, DataSource>
        implements DataSourceService, ApplicationListener<ServletWebServerInitializedEvent> {

    @Resource
    private SourceManager sourceManager;
    @Resource
    private PasswordEncAndDecService passwordEncAndDecService;
    @Resource
    private FlowEngine flowEngine;

    /**
     * 加载数据源
     *
     * @param event 事件
     */
    @Override
    public void onApplicationEvent(@NonNull ServletWebServerInitializedEvent event) {
        List<DataSource> dataSources = this.lambdaQuery()
                .eq(DataSource::getStatus, Status.ENABLE.name())
                .list();
        if (CollUtil.isEmpty(dataSources)) {
            log.warn("没有查询到任何启用状态的数据源");
            return;
        }
        MDCUtils.setRequestId(IdUtils.getSimpleId());
        try {
            for (DataSource dataSource : dataSources) {
                try {
                    this.load(dataSource);
                } catch (Exception e) {
                    log.warn("加载数据源失败: {}", dataSource.getName(), e);
                }
            }
            log.info("数据源加载完成");
        } finally {
            MDC.clear();
        }
    }


    /**
     * 加载数据源
     *
     * @param dataSourceCode 数据源编码
     */
    @Override
    public void load(String workspaceCode, String dataSourceCode) {
        DataSource dataSource = this.lambdaQuery()
                .eq(DataSource::getWorkspaceCode, workspaceCode)
                .eq(DataSource::getCode, dataSourceCode)
                .eq(DataSource::getStatus, Status.ENABLE.name())
                .one();
        if (dataSource == null) {
            throw new DataSourceException("数据源不存在");
        }
        this.load(dataSource);
    }

    /**
     * 加载数据源
     *
     * @param dataSource 数据源
     */
    @SuppressWarnings("ExtractMethodRecommender")
    @SneakyThrows
    private void load(DataSource dataSource) {
        if (dataSource == null) {
            return;
        }
        if (Objects.equals(dataSource.getStatus(), Status.DISABLE.name())) {
            throw new IllegalArgumentException("数据源已禁用");
        }
        log.info("加载数据源: {}({})", dataSource.getName(), dataSource.getCode());
        String password = this.passwordEncAndDecService.decrypt(dataSource.getPassword());
        DataSourceType sourceType = DataSourceType.getByType(dataSource.getDataSourceType());
        // 是JdbcSource数据源
        if (sourceType.isJdbc()) {
            Class<? extends Source> sourceClass = sourceType.getSourceClass();
            Constructor<? extends Source> constructor = sourceClass.getConstructor();
            Source source = constructor.newInstance();
            if (source instanceof JdbcSource jdbcSource) {
                jdbcSource.setName(dataSource.getName());
                jdbcSource.setCode(dataSource.getCode());
                jdbcSource.setUrl(dataSource.getUrl());
                jdbcSource.setUsername(dataSource.getUsername());
                jdbcSource.setPassword(password);
                jdbcSource.setDriverClassName(dataSource.getDriver());
                jdbcSource.setProperties(dataSource.getProperties());
                jdbcSource.setPartitioningAlgorithm(dataSource.getPartitioningAlgorithm());
                jdbcSource.setIsEnableHealth(Objects.equals(dataSource.getHealthCheck(), Status.ENABLE.name()));
                this.sourceManager.add(dataSource.getWorkspaceCode(), jdbcSource);
            } else {
                throw new DataSourceException("数据源类型错误, 不是 JDBC 数据源");
            }
        } else {
            switch (sourceType) {
                case ELASTIC:
                    ElasticDataSource elasticDataSource = new ElasticDataSource();
                    elasticDataSource.setName(dataSource.getName());
                    elasticDataSource.setCode(dataSource.getCode());
                    elasticDataSource.setUrl(dataSource.getUrl());
                    elasticDataSource.setUsername(dataSource.getUsername());
                    elasticDataSource.setPassword(password);
                    elasticDataSource.setIsEnableHealth(Objects.equals(dataSource.getHealthCheck(), Status.ENABLE.name()));
                    elasticDataSource.setProperties(dataSource.getProperties());
                    this.sourceManager.add(dataSource.getWorkspaceCode(), elasticDataSource);
                    break;
                case KAFKA:
                    KafkaDataSource kafkaDataSource = new KafkaDataSource();
                    kafkaDataSource.setName(dataSource.getName());
                    kafkaDataSource.setCode(dataSource.getCode());
                    kafkaDataSource.setUrl(dataSource.getUrl());
                    kafkaDataSource.setUsername(dataSource.getUsername());
                    kafkaDataSource.setPassword(password);
                    kafkaDataSource.setIsEnableHealth(Objects.equals(dataSource.getHealthCheck(), Status.ENABLE.name()));
                    kafkaDataSource.setProperties(dataSource.getProperties());
                    this.sourceManager.add(dataSource.getWorkspaceCode(), kafkaDataSource);
                    break;
                default:
                    log.warn("暂不支持的数据源类型: {}", dataSource.getDataSourceType());
            }
        }
    }

    /**
     * 移除数据源
     *
     * @param dataSourceCode 数据源编码
     */
    @Override
    public void remove(String workspaceCode, String dataSourceCode) {
        // 停止所有启用中的数据流
        List<Flow> allFlows = this.flowEngine.getAll();
        for (Flow flow : allFlows) {
            if (flow.getWorkspaceCode().equals(workspaceCode)
                    && flow.getCode().contains(dataSourceCode)) {
                // 设置MDC, 以便日志中能看到是哪个数据流
                MDC.put(Constant.FLOW_CODE, flow.getCode());
                log.info("因移除数据源开始停止使用此数据源的数据流: {}({})", flow.getName(), flow.getCode());
                try {
                    // 移除并停止数据流
                    this.flowEngine.remove(flow.getWorkspaceCode(), flow.getCode());
                } catch (Exception e) {
                    log.error("移除数据流失败: {}({})", flow.getName(), flow.getCode(), e);
                } finally {
                    MDC.remove(Constant.FLOW_CODE);
                }
            }
        }
        this.sourceManager.remove(workspaceCode, dataSourceCode);
    }

}