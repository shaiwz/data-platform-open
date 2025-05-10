/*
 * ============================================================================
 *
 *                    数海文舟 (DATA PLATFORM) 版权所有 © 2025
 *
 *       本软件受著作权法和国际版权条约保护。
 *       未经明确书面授权，任何单位或个人不得对本软件进行复制、修改、分发、
 *       逆向工程、商业用途等任何形式的非法使用。违者将面临人民币100万元的
 *       法定罚款及可能的法律追责。
 *
 *       举报侵权行为可获得实际罚款金额40%的现金奖励。
 *       法务邮箱：761945125@qq.com
 *
 *       COPYRIGHT (C) 2025 dingqianwen COMPANY. ALL RIGHTS RESERVED.
 *
 * ============================================================================
 */
package cn.dataplatform.open.web.service.datasource.impl;


import cn.dataplatform.open.common.body.DataSourceMessageBody;
import cn.dataplatform.open.common.component.OrikaMapper;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.event.DataSourceEvent;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.common.vo.base.PageBase;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.annotation.OperationLog;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.enums.OperationLogAction;
import cn.dataplatform.open.web.enums.OperationLogFunction;
import cn.dataplatform.open.web.service.PasswordEncAndDecService;
import cn.dataplatform.open.web.service.datasource.DataSourceService;
import cn.dataplatform.open.web.service.datasource.tables.DataSourceTable;
import cn.dataplatform.open.web.service.datasource.tables.DataSourceTableFactory;
import cn.dataplatform.open.web.service.datasource.test.DataSourceTest;
import cn.dataplatform.open.web.service.datasource.test.DataSourceTestFactory;
import cn.dataplatform.open.web.store.entity.DataFlow;
import cn.dataplatform.open.web.store.entity.DataFlowPublish;
import cn.dataplatform.open.web.store.entity.DataSource;
import cn.dataplatform.open.web.store.mapper.DataFlowMapper;
import cn.dataplatform.open.web.store.mapper.DataFlowPublishMapper;
import cn.dataplatform.open.web.store.mapper.DataSourceMapper;
import cn.dataplatform.open.web.vo.data.source.*;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.dataplatform.open.web.vo.workspace.WorkspaceData;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Closeable;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@Slf4j
@Service
public class DataSourceServiceImpl extends ServiceImpl<DataSourceMapper, DataSource> implements DataSourceService {

    public static final String SCHEMA = "schema";

    /**
     * 默认半小时自动过期
     */
    public TimedCache<String, Object> defaultDataSourceCache = CacheUtil.newTimedCache(1800 * 1000L);

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private DataFlowMapper dataFlowMapper;
    @Resource
    private DataFlowPublishMapper dataFlowPublishMapper;
    @Resource
    private OrikaMapper orikaMapper;
    @Resource
    private PasswordEncAndDecService passwordEncAndDecService;

    /**
     * 默认数据源缓存
     */
    public DataSourceServiceImpl() {
        this.defaultDataSourceCache.setListener((key, value) -> {
            log.info("缓存过期:{}", key);
            if (value instanceof HikariDataSource hikariDataSource) {
                IoUtil.close(hikariDataSource);
            } else if (value instanceof ShardingSphereDataSource sphereDataSource) {
                IoUtil.close(sphereDataSource);
            } else if (value instanceof RabbitTemplate rabbitTemplate) {
                rabbitTemplate.stop();
            } else if (value instanceof AdminClient adminClient) {
                IoUtil.close(adminClient);
            } else if (value instanceof Closeable closeable) {
                IoUtil.close(closeable);
            }
        });
    }

    /**
     * 数据源列表
     *
     * @param pageRequest p
     * @return r
     */
    @Override
    public PageResult<DataSourceListResponse> list(PageRequest<DataSourceListRequest> pageRequest) {
        PageBase page = pageRequest.getPage();
        WorkspaceData workspace = Context.getWorkspace();
        DataSourceListRequest query = Optional.ofNullable(pageRequest.getQuery()).orElse(new DataSourceListRequest());
        Page<DataSource> dataSourcePage = this.lambdaQuery().like(StrUtil.isNotBlank(query.getName()), DataSource::getName, query.getName())
                .eq(StrUtil.isNotBlank(query.getCode()), DataSource::getCode, query.getCode())
                .eq(StrUtil.isNotBlank(query.getStatus()), DataSource::getStatus, query.getStatus())
                .eq(StrUtil.isNotBlank(query.getType()), DataSource::getType, query.getType())
                .in(CollUtil.isNotEmpty(query.getTypes()), DataSource::getType, query.getTypes())
                .eq(DataSource::getWorkspaceCode, workspace.getCode())
                .orderByDesc(DataSource::getStatus, DataSource::getCreateTime)
                .page(new Page<>(page.getCurrent(), page.getSize()));
        PageResult<DataSourceListResponse> pageResult = new PageResult<>();
        List<DataSourceListResponse> collect = dataSourcePage.getRecords()
                .stream()
                .map(m -> {
                    DataSourceListResponse dataSourceListResponse = new DataSourceListResponse();
                    this.orikaMapper.map(m, dataSourceListResponse);
                    return dataSourceListResponse;
                }).collect(Collectors.toList());
        pageResult.setData(collect, dataSourcePage.getCurrent(), dataSourcePage.getSize(), dataSourcePage.getTotal());
        return pageResult;
    }

    /**
     * 添加数据源
     *
     * @param dataSourceAddRequest d
     * @return r
     */
    @OperationLog(function = OperationLogFunction.DATASOURCE, action = OperationLogAction.ADD)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(DataSourceAddRequest dataSourceAddRequest) {
        // 检查名称是否存在
        if (this.lambdaQuery().eq(DataSource::getName, dataSourceAddRequest.getName())
                .eq(DataSource::getWorkspaceCode, Context.getWorkspace().getCode())
                .exists()) {
            throw new ApiException("数据源名称已存在");
        }
        UserData user = Context.getUser();
        WorkspaceData workspace = Context.getWorkspace();
        DataSource dataSource = new DataSource();
        this.orikaMapper.map(dataSourceAddRequest, dataSource);
        dataSource.setCode(UUID.fastUUID().toString(true));
        dataSource.setCreateUserId(user.getId());
        dataSource.setWorkspaceCode(workspace.getCode());
        dataSource.setMaskColumn(JSON.toJSONString(dataSourceAddRequest.getMaskColumn()));
        if (StrUtil.isNotBlank(dataSourceAddRequest.getPassword())) {
            dataSource.setPassword(this.passwordEncAndDecService.encrypt(dataSourceAddRequest.getPassword()));
        }
        this.save(dataSource);
        if (dataSource.getStatus().equals(Status.ENABLE.name())) {
            DataSourceMessageBody messageBody = new DataSourceMessageBody();
            messageBody.setType(DataSourceMessageBody.Type.LOAD);
            messageBody.setId(dataSource.getId());
            messageBody.setWorkspaceCode(dataSource.getWorkspaceCode());
            this.applicationEventPublisher.publishEvent(new DataSourceEvent(messageBody));
        }
        return true;
    }

    /**
     * 修改数据源
     *
     * @param dataSourceUpdateRequest d
     * @return r
     */
    @OperationLog(function = OperationLogFunction.DATASOURCE, action = OperationLogAction.UPDATE)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DataSourceUpdateRequest dataSourceUpdateRequest) {
        // 名称是否存在
        if (this.lambdaQuery().eq(DataSource::getName, dataSourceUpdateRequest.getName())
                .ne(DataSource::getId, dataSourceUpdateRequest.getId())
                .eq(DataSource::getWorkspaceCode, Context.getWorkspace().getCode())
                .exists()) {
            throw new ApiException("数据源名称已存在");
        }
        Long id = dataSourceUpdateRequest.getId();
        DataSource dataSource = this.getById(id);
        if (dataSource == null) {
            throw new ApiException("数据源不存在");
        }
        // 如果是关闭改为启用
        if (Objects.equals(dataSource.getStatus(), Status.DISABLE.name())
                && Objects.equals(dataSourceUpdateRequest.getStatus(), Status.ENABLE.name())) {
            DataSourceMessageBody messageBody = new DataSourceMessageBody();
            messageBody.setType(DataSourceMessageBody.Type.LOAD);
            messageBody.setId(dataSource.getId());
            messageBody.setWorkspaceCode(dataSource.getWorkspaceCode());
            this.applicationEventPublisher.publishEvent(new DataSourceEvent(messageBody));
        } else if (Objects.equals(dataSource.getStatus(), Status.ENABLE.name())
                && Objects.equals(dataSourceUpdateRequest.getStatus(), Status.DISABLE.name())) {
            // 如果是启用改为关闭
            DataSourceMessageBody messageBody = new DataSourceMessageBody();
            messageBody.setType(DataSourceMessageBody.Type.REMOVE);
            messageBody.setId(dataSource.getId());
            messageBody.setWorkspaceCode(dataSource.getWorkspaceCode());
            this.applicationEventPublisher.publishEvent(new DataSourceEvent(messageBody));
        } else {
            // 更新数据源
            if (Objects.equals(dataSourceUpdateRequest.getStatus(), Status.ENABLE.name())) {
                DataSourceMessageBody messageBody = new DataSourceMessageBody();
                messageBody.setType(DataSourceMessageBody.Type.UPDATE);
                messageBody.setId(dataSource.getId());
                messageBody.setWorkspaceCode(dataSource.getWorkspaceCode());
                this.applicationEventPublisher.publishEvent(new DataSourceEvent(messageBody));
            }
            // 一直关闭时更新忽略
        }
        this.defaultDataSourceCache.remove(dataSource.getCode());
        if (StrUtil.isNotBlank(dataSourceUpdateRequest.getPassword())) {
            // 如果编辑时，源密码与数据库密钥一致，则不需要再次加密，否则是新设置的密码，修改过
            if (!Objects.equals(dataSourceUpdateRequest.getPassword(), dataSource.getPassword())) {
                // 加密后存储
                dataSourceUpdateRequest.setPassword(this.passwordEncAndDecService.encrypt(dataSourceUpdateRequest.getPassword()));
            }
        }
        this.orikaMapper.map(dataSourceUpdateRequest, dataSource);
        dataSource.setMaskColumn(JSON.toJSONString(dataSourceUpdateRequest.getMaskColumn()));
        this.updateById(dataSource);
        return true;
    }

    /**
     * 删除数据源
     *
     * @param id 数据源ID
     * @return r
     */
    @OperationLog(function = OperationLogFunction.DATASOURCE, action = OperationLogAction.DELETE)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean delete(Long id) {
        DataSource dataSource = this.getById(id);
        if (dataSource == null) {
            throw new ApiException("数据源不存在");
        }
        // 是否被数据流引用
        DataFlow dataFlow = this.dataFlowMapper.refDataSourceCode(dataSource.getWorkspaceCode(), dataSource.getCode());
        if (dataFlow != null) {
            throw new ApiException("数据源被数据流引用：" + dataFlow.getName());
        }
        // 判断有没有被已发布的数据流引用
        DataFlowPublish dataFlowPublish = this.dataFlowPublishMapper.refDataSourceCode(dataSource.getWorkspaceCode(), dataSource.getCode());
        if (dataFlowPublish != null) {
            throw new ApiException("数据源被发布的数据流引用：" + dataFlowPublish.getName());
        }
        this.removeById(id);
        // 如果是启用状态,需要通知删除
        if (Objects.equals(dataSource.getStatus(), Status.ENABLE.name())) {
            DataSourceMessageBody messageBody = new DataSourceMessageBody();
            messageBody.setType(DataSourceMessageBody.Type.REMOVE);
            messageBody.setId(id);
            messageBody.setWorkspaceCode(dataSource.getWorkspaceCode());
            this.applicationEventPublisher.publishEvent(new DataSourceEvent(messageBody));
        }
        this.defaultDataSourceCache.remove(dataSource.getCode());
        return true;
    }

    /**
     * 数据源详情
     *
     * @param id id
     * @return r
     */
    @Override
    public DataSourceDetailResponse detail(Long id) {
        DataSource dataSource = this.getById(id);
        if (dataSource == null) {
            throw new ApiException("数据源不存在");
        }
        DataSourceDetailResponse dataSourceDetailResponse = new DataSourceDetailResponse();
        this.orikaMapper.map(dataSource, dataSourceDetailResponse);
        if (StrUtil.isNotBlank(dataSource.getMaskColumn())) {
            List<MarkColumn> markColumns = JSON.parseArray(dataSource.getMaskColumn(), MarkColumn.class);
            dataSourceDetailResponse.setMaskColumn(markColumns == null ? Collections.emptyList() : markColumns);
        }
        return dataSourceDetailResponse;
    }


    /**
     * 测试数据源
     *
     * @param dataSourceTestRequest d
     * @return r
     */
    @Override
    public Boolean test(DataSourceTestRequest dataSourceTestRequest) {
        Long id = dataSourceTestRequest.getId();
        if (id != null) {
            // 已经创建过的数据源测试链接
            DataSource dataSource = this.getById(id);
            if (dataSource == null) {
                throw new ApiException("数据源不存在");
            }
            // 详情返回的加密后的密码
            if (StrUtil.isNotBlank(dataSourceTestRequest.getPassword()) &&
                    Objects.equals(dataSourceTestRequest.getPassword(), dataSource.getPassword())) {
                String decrypt = this.passwordEncAndDecService.decrypt(dataSource.getPassword());
                dataSourceTestRequest.setPassword(decrypt);
            }
        }
        DataSourceTest dataSourceTest = DataSourceTestFactory.get(dataSourceTestRequest.getType());
        String url = dataSourceTestRequest.getUrl();
        String username = dataSourceTestRequest.getUsername();
        String password = dataSourceTestRequest.getPassword();
        return dataSourceTest.testConnection(url, username, password);
    }

    /**
     * 获取数据源下所有的表
     *
     * @param request id
     * @return r
     */
    @SneakyThrows
    @Override
    public List<SchemaTableMap> listSchemaTable(ListSchemaTableRequest request) {
        Long id = request.getId();
        DataSource dataSource;
        if (id != null) {
            dataSource = this.getById(id);
        } else {
            dataSource = this.lambdaQuery().eq(DataSource::getCode, request.getCode()).one();
        }
        if (dataSource == null) {
            throw new ApiException("数据源不存在");
        }
        if (!Objects.equals(dataSource.getStatus(), Status.ENABLE.name())) {
            throw new ApiException("数据源非启用状态");
        }
        javax.sql.DataSource ds = (javax.sql.DataSource) this.dataSourceConnect(dataSource);
        try (Connection connection = ds.getConnection()) {
            DataSourceTable dataSourceTable = DataSourceTableFactory.get(dataSource.getType());
            List<SchemaTable> schemaTables = dataSourceTable.schemaTable(connection);
            Map<String, List<SchemaTable>> collected = schemaTables.stream().collect(Collectors.groupingBy(SchemaTable::getSchema));
            return collected.entrySet().stream().map(m -> {
                SchemaTableMap schemaTableMap = new SchemaTableMap();
                schemaTableMap.setKey(m.getKey());
                schemaTableMap.setTag(SCHEMA);
                schemaTableMap.setLabel(m.getKey());
                List<SchemaTable> value = m.getValue();
                schemaTableMap.setChildren(value.stream().map(b -> {
                    SchemaTableMap.Children schemaTableBody = new SchemaTableMap.Children();
                    schemaTableBody.setKey(b.getTable());
                    schemaTableBody.setSchema(b.getSchema());
                    if (StrUtil.isBlank(b.getComment())) {
                        schemaTableBody.setLabel(b.getTable());
                    } else {
                        schemaTableBody.setLabel(String.format("%s(%s)", b.getTable(), b.getComment()));
                    }
                    return schemaTableBody;
                }).collect(Collectors.toList()));
                return schemaTableMap;
            }).collect(Collectors.toList());
        }
    }

    /**
     * 获取连接,先从缓存获取
     *
     * @param dataSource 数据库连接配置
     * @return 数据源
     */
    @Override
    public Object dataSourceConnect(DataSource dataSource) {
        String code = dataSource.getCode();
        String type = dataSource.getType();
        String url = dataSource.getUrl();
        String username = dataSource.getUsername();
        // 解密
        String password = this.passwordEncAndDecService.decrypt(dataSource.getPassword());
        return this.defaultDataSourceCache.get(code, () -> {
            if (type.equals("RabbitMQ")) {
                CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
                connectionFactory.setUri(url);
                connectionFactory.setUsername(username);
                connectionFactory.setPassword(password);
                connectionFactory.setVirtualHost("/");
                return new RabbitTemplate(connectionFactory);
            } else if (type.equals("Kafka")) {
                Properties props = new Properties();
                props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, url);
                // 设置 SASL 认证相关配置
                if (StrUtil.isNotBlank(username) && StrUtil.isNotBlank(password)) {
                    props.put("security.protocol", "SASL_SSL");
                    props.put("sasl.mechanism", "PLAIN");
                    props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                            "username=\"" + username + "\" " +
                            "password=\"" + password + "\";");
                }
                return AdminClient.create(props);
            }
            HikariDataSource hikariDataSource = new HikariDataSource();
            hikariDataSource.setJdbcUrl(dataSource.getUrl());
            hikariDataSource.setUsername(dataSource.getUsername());
            hikariDataSource.setPassword(password);
            hikariDataSource.setDriverClassName(dataSource.getDriver());
            hikariDataSource.setMinimumIdle(5);
            hikariDataSource.setMaximumPoolSize(dataSource.getMaxPoolSize());
            return hikariDataSource;
        });
    }

    /**
     * 获取数据源表信息
     *
     * @param request id
     * @return r
     */
    @SneakyThrows
    @Override
    public TableDetail tableDetail(TableDetailRequest request) {
        Long id = request.getId();
        DataSource dataSource = this.getById(id);
        if (dataSource == null) {
            throw new ApiException("数据源不存在");
        }
        // 是否启用
        if (!Objects.equals(dataSource.getStatus(), Status.ENABLE.name())) {
            throw new ApiException("数据源非启用状态");
        }
        String schema = request.getSchema();
        String table = request.getTable();
        javax.sql.DataSource ds = (javax.sql.DataSource) this.dataSourceConnect(dataSource);
        try (Connection connection = ds.getConnection()) {
            DataSourceTable dataSourceTable = DataSourceTableFactory.get(dataSource.getType());
            return dataSourceTable.tableDetail(connection, schema, table);
        }
    }

}
