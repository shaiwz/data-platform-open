package cn.dataplatform.open.flow.service.impl;

import cn.dataplatform.open.common.enums.DataSourceType;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.source.*;
import cn.dataplatform.open.flow.service.DataSourceService;
import cn.dataplatform.open.flow.service.PasswordEncAndDecService;
import cn.dataplatform.open.flow.store.entity.DataSource;
import cn.dataplatform.open.flow.store.mapper.DataSourceMapper;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
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
        for (DataSource dataSource : dataSources) {
            try {
                this.load(dataSource);
            } catch (Exception e) {
                log.warn("加载数据源失败:{}", dataSource.getName(), e);
            }
        }
    }


    /**
     * 加载数据源
     *
     * @param id 数据源ID
     */
    @Override
    public void load(Long id) {
        DataSource dataSource = this.getById(id);
        if (dataSource == null) {
            throw new IllegalArgumentException("数据源不存在");
        }
        this.load(dataSource);
    }

    /**
     * 加载数据源
     *
     * @param dataSource 数据源
     */
    private void load(DataSource dataSource) {
        if (dataSource == null) {
            return;
        }
        if (dataSource.getStatus().equals(Status.DISABLE.name())) {
            throw new IllegalArgumentException("数据源已禁用");
        }
        log.info("加载数据源:{}({})", dataSource.getName(), dataSource.getCode());
        String password = this.passwordEncAndDecService.decrypt(dataSource.getPassword());
        DataSourceType sourceType = DataSourceType.getByType(dataSource.getType());
        switch (sourceType) {
            case MYSQL:
                MySQLDataSource sqlDataSource = new MySQLDataSource();
                sqlDataSource.setCode(dataSource.getCode());
                sqlDataSource.setUrl(dataSource.getUrl());
                sqlDataSource.setUsername(dataSource.getUsername());
                sqlDataSource.setPassword(password);
                sqlDataSource.setDriverClassName(dataSource.getDriver());
                sqlDataSource.setPartitioningAlgorithm(dataSource.getPartitioningAlgorithm());
                sqlDataSource.setMaxPoolSize(dataSource.getMaxPoolSize());
                this.sourceManager.addSource(dataSource.getWorkspaceCode(), sqlDataSource);
                break;
            case DORIS:
                DorisDataSource dorisDataSource = new DorisDataSource();
                dorisDataSource.setCode(dataSource.getCode());
                dorisDataSource.setUrl(dataSource.getUrl());
                dorisDataSource.setUsername(dataSource.getUsername());
                dorisDataSource.setPassword(password);
                dorisDataSource.setDriverClassName(dataSource.getDriver());
                if (StrUtil.isNotBlank(dataSource.getBeNodes())) {
                    dorisDataSource.setBeNodes(Arrays.asList(dataSource.getBeNodes().split(",")));
                }
                if (StrUtil.isNotBlank(dataSource.getFeNodes())) {
                    dorisDataSource.setFeNodes(Arrays.asList(dataSource.getFeNodes().split(",")));
                }
                dorisDataSource.setMaxPoolSize(dataSource.getMaxPoolSize());
                this.sourceManager.addSource(dataSource.getWorkspaceCode(), dorisDataSource);
                break;
            case ELASTIC:
                ElasticDataSource elasticDataSource = new ElasticDataSource();
                elasticDataSource.setCode(dataSource.getCode());
                elasticDataSource.setUrl(dataSource.getUrl());
                elasticDataSource.setUsername(dataSource.getUsername());
                elasticDataSource.setPassword(password);
                this.sourceManager.addSource(dataSource.getWorkspaceCode(), elasticDataSource);
                break;
            case KAFKA:
                KafkaDataSource kafkaDataSource = new KafkaDataSource();
                kafkaDataSource.setCode(dataSource.getCode());
                kafkaDataSource.setUrl(dataSource.getUrl());
                kafkaDataSource.setUsername(dataSource.getUsername());
                kafkaDataSource.setPassword(password);
                this.sourceManager.addSource(dataSource.getWorkspaceCode(), kafkaDataSource);
                break;
            case POSTGRESQL:
                PostgreSQLDataSource postgreSQLDataSource = new PostgreSQLDataSource();
                postgreSQLDataSource.setName(dataSource.getName());
                postgreSQLDataSource.setCode(dataSource.getCode());
                postgreSQLDataSource.setUrl(dataSource.getUrl());
                postgreSQLDataSource.setUsername(dataSource.getUsername());
                postgreSQLDataSource.setPassword(password);
                postgreSQLDataSource.setDriverClassName(dataSource.getDriver());
                postgreSQLDataSource.setPartitioningAlgorithm(dataSource.getPartitioningAlgorithm());
                postgreSQLDataSource.setMaxPoolSize(dataSource.getMaxPoolSize());
                this.sourceManager.addSource(dataSource.getWorkspaceCode(), postgreSQLDataSource);
                break;
            default:
                throw new UnsupportedOperationException("暂不支持的数据源类型:" + dataSource.getType());
        }
    }

    /**
     * 移除数据源
     *
     * @param id 数据源ID
     */
    @Override
    public void remove(Long id) {
        DataSource dataSource = this.getById(id);
        if (dataSource == null) {
            throw new IllegalArgumentException("数据源不存在");
        }
        Source source = this.sourceManager.removeSource(dataSource.getWorkspaceCode(), dataSource.getCode());
        IoUtil.close(source);
        log.info("移除数据源:{}({})", dataSource.getName(), dataSource.getCode());
    }

}
