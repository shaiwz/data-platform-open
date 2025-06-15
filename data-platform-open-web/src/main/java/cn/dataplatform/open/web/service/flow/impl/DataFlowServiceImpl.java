package cn.dataplatform.open.web.service.flow.impl;

import cn.dataplatform.open.common.body.DataFlowMessageBody;
import cn.dataplatform.open.common.component.OrikaMapper;
import cn.dataplatform.open.common.constant.ServerConstant;
import cn.dataplatform.open.common.enums.RedisKey;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.enums.flow.FlowStatus;
import cn.dataplatform.open.common.event.DataFlowEvent;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.common.server.Server;
import cn.dataplatform.open.common.server.ServerManager;
import cn.dataplatform.open.common.util.CronUtils;
import cn.dataplatform.open.common.util.ValidationUtils;
import cn.dataplatform.open.common.util.VersionUtils;
import cn.dataplatform.open.common.vo.base.PageBase;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.common.vo.flow.Design;
import cn.dataplatform.open.common.vo.flow.FlowError;
import cn.dataplatform.open.common.vo.flow.FlowHeartbeat;
import cn.dataplatform.open.web.annotation.OperationLog;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.enums.OperationLogAction;
import cn.dataplatform.open.web.enums.OperationLogFunction;
import cn.dataplatform.open.web.service.OperationLogService;
import cn.dataplatform.open.web.service.UserService;
import cn.dataplatform.open.web.service.datasource.DataSourceService;
import cn.dataplatform.open.web.service.flow.DataFlowPublishService;
import cn.dataplatform.open.web.service.flow.DataFlowService;
import cn.dataplatform.open.web.store.entity.DataFlow;
import cn.dataplatform.open.web.store.entity.DataFlowPublish;
import cn.dataplatform.open.web.store.entity.DataSource;
import cn.dataplatform.open.web.store.entity.User;
import cn.dataplatform.open.web.store.mapper.DataFlowMapper;
import cn.dataplatform.open.web.vo.data.flow.*;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.dataplatform.open.web.vo.workspace.WorkspaceData;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jayway.jsonpath.JsonPath;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/4
 * @since 1.0.0
 */
@Service
public class DataFlowServiceImpl extends ServiceImpl<DataFlowMapper, DataFlow> implements DataFlowService {

    public static final String JSON_PATH_DATASOURCE_CODE = "$..datasourceCode";
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private DataFlowPublishService dataFlowPublishService;
    @Resource
    private ServerManager serverManager;
    @Resource
    private OrikaMapper orikaMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private DataSourceService dataSourceService;

    /**
     * 数据流列表
     *
     * @param pageRequest p
     * @return r
     */
    @Override
    public PageResult<DataFlowListResponse> list(PageRequest<DataFlowListRequest> pageRequest) {
        WorkspaceData workspace = Context.getWorkspace();
        PageBase page = pageRequest.getPage();
        DataFlowListRequest query = pageRequest.getQuery();
        Page<DataFlow> dataFlowPage = this.lambdaQuery()
                .and(StrUtil.isNotBlank(query.getValue()), q -> q
                        .like(DataFlow::getName, query.getValue())
                        .or()
                        .like(DataFlow::getCode, query.getValue())
                )
                .like(StrUtil.isNotBlank(query.getName()), DataFlow::getName, query.getName())
                .eq(StrUtil.isNotBlank(query.getCode()), DataFlow::getCode, query.getCode())
                .eq(DataFlow::getWorkspaceCode, workspace.getCode())
                .last("ORDER BY CASE status " +
                        "WHEN 'ENABLE' THEN 1 " +
                        "WHEN 'PAUSE' THEN 2 " +
                        "WHEN 'TBP' THEN 3 " +
                        "ELSE 4 END ASC, update_time DESC")
                .page(new Page<>(page.getCurrent(), page.getSize()));
        PageResult<DataFlowListResponse> pageResult = new PageResult<>();
        List<DataFlow> records = dataFlowPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            pageResult.setData(CollUtil.newArrayList(), page.getCurrent(), page.getSize(), 0L);
            return pageResult;
        }
        List<Long> flowIds = records.stream().map(DataFlow::getId).toList();
        List<cn.dataplatform.open.web.store.entity.OperationLog> operationLogs = this.operationLogService.lambdaQuery()
                .select(cn.dataplatform.open.web.store.entity.OperationLog::getUserId, cn.dataplatform.open.web.store.entity.OperationLog::getRecordId)
                .eq(cn.dataplatform.open.web.store.entity.OperationLog::getWorkspaceCode, workspace.getCode())
                .eq(cn.dataplatform.open.web.store.entity.OperationLog::getFunction, OperationLogFunction.DATA_FLOW.name())
                .in(cn.dataplatform.open.web.store.entity.OperationLog::getRecordId, flowIds)
                .groupBy(cn.dataplatform.open.web.store.entity.OperationLog::getUserId, cn.dataplatform.open.web.store.entity.OperationLog::getRecordId)
                .list();
        // 按照数据流id分组 value是user list
        Map<Long, List<Long>> map = operationLogs.stream().collect(Collectors.groupingBy(cn.dataplatform.open.web.store.entity.OperationLog::getRecordId,
                Collectors.mapping(cn.dataplatform.open.web.store.entity.OperationLog::getUserId, Collectors.toList())));
        Set<Long> userIds = operationLogs.stream().map(cn.dataplatform.open.web.store.entity.OperationLog::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap;
        if (CollUtil.isNotEmpty(userIds)) {
            List<User> list = this.userService.lambdaQuery()
                    .select(User::getId, User::getUsername, User::getAvatar)
                    .in(User::getId, userIds).list();
            userMap = list.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        } else {
            userMap = new HashMap<>();
        }
        // publish
        List<String> codes = records.stream().map(DataFlow::getCode).toList();
        List<DataFlowPublish> dataFlowPublishes = this.dataFlowPublishService.lambdaQuery()
                .eq(DataFlowPublish::getWorkspaceCode, workspace.getCode())
                .in(DataFlowPublish::getCode, codes)
                .in(DataFlowPublish::getStatus, Arrays.asList(FlowStatus.ENABLE.name(), FlowStatus.PAUSE.name()))
                .list();
        // 按照code+版本to map
        Map<String, DataFlowPublish> dataFlowPublishMap = dataFlowPublishes.stream()
                .collect(Collectors.toMap(m -> String.format("%s-%s", m.getCode(), m.getVersion()), Function.identity()));
        Long currentUserId = Context.getUser().getId();
        List<DataFlowListResponse> collect = records.parallelStream()
                .map(m -> {
                    DataFlowListResponse dataFlowListResponse = new DataFlowListResponse();
                    this.orikaMapper.map(m, dataFlowListResponse);
                    // 编辑过的用户
                    List<Long> uIds = map.get(m.getId());
                    if (CollUtil.isEmpty(uIds)) {
                        uIds = new ArrayList<>();
                    } else {
                        uIds.remove(m.getCreateUserId());
                        // 去重复
                        uIds = uIds.stream().distinct().collect(Collectors.toList());
                    }
                    // 把创建者放在第一个
                    uIds.addFirst(m.getCreateUserId());
                    // 如果有当前登录用户，登录用户放在第二个
                    if (uIds.contains(currentUserId) && !Objects.equals(m.getCreateUserId(), currentUserId)) {
                        // 如果只有一个，不需要调整
                        if (uIds.size() > 2) {
                            uIds.remove(currentUserId);
                            uIds.add(1, currentUserId);
                        }
                    }
                    // 最多返回6个
                    uIds = CollUtil.sub(uIds, 0, 6);
                    List<UserData> users = uIds.stream().map(userMap::get)
                            .filter(Objects::nonNull)
                            .map(u -> {
                                UserData user = new UserData();
                                this.orikaMapper.map(u, user);
                                return user;
                            })
                            .collect(Collectors.toList());
                    dataFlowListResponse.setUsers(users);
                    // 已发布的
                    DataFlowPublish dataFlowPublish = dataFlowPublishMap.get(String.format("%s-%s", m.getCode(), m.getPublishVersion()));
                    if (dataFlowPublish != null) {
                        String key = String.format("%s-%s", m.getWorkspaceCode(), m.getCode());
                        dataFlowListResponse.setPublishId(dataFlowPublish.getId());
                        // 查询flow服务是否被标记为执行异常,异常则标记为异常状态
                        RList<FlowError> flowErrors = this.redissonClient.getList(RedisKey.FLOW_ERROR.build(key));
                        if (flowErrors.isExists()) {
                            dataFlowListResponse.setFlowErrors(flowErrors.readAll());
                        }
                        RMap<String, FlowHeartbeat> rMap = this.redissonClient.getMap(RedisKey.FLOW_HEARTBEAT.build(key));
                        dataFlowListResponse.setFlowHeartbeats(rMap.values());
                    }
                    return dataFlowListResponse;
                }).collect(Collectors.toList());
        pageResult.setData(collect, dataFlowPage.getCurrent(), dataFlowPage.getSize(), dataFlowPage.getTotal());
        return pageResult;
    }

    /**
     * 创建数据流
     *
     * @param dataFlowListResponse d
     * @return r
     */
    @OperationLog(function = OperationLogFunction.DATA_FLOW, action = OperationLogAction.ADD,
            requestExtractId = false, id = "#id")
    @Override
    public DataFlowCreateResponse create(DataFlowCreateRequest dataFlowListResponse) {
        WorkspaceData workspace = Context.getWorkspace();
        // 检查名称是否重复
        if (this.lambdaQuery().eq(DataFlow::getName, dataFlowListResponse.getName())
                .eq(DataFlow::getWorkspaceCode, workspace.getCode())
                .exists()) {
            throw new ApiException("数据流名称已经存在");
        }
        DataFlow dataFlow = new DataFlow();
        this.orikaMapper.map(dataFlowListResponse, dataFlow);
        dataFlow.setCode(UUID.fastUUID().toString(true));
        dataFlow.setCreateUserId(Context.getUser().getId());
        dataFlow.setStatus(FlowStatus.TBP.name());
        dataFlow.setWorkspaceCode(workspace.getCode());
        dataFlow.setCurrentVersion(VersionUtils.INIT_VERSION);
        this.save(dataFlow);
        DataFlowCreateResponse dataFlowCreateResponse = new DataFlowCreateResponse();
        dataFlowCreateResponse.setId(dataFlow.getId());
        dataFlowCreateResponse.setCode(dataFlow.getCode());
        return dataFlowCreateResponse;
    }

    /**
     * 更新数据流
     *
     * @param dataFlowUpdateRequest d
     * @return r
     */
    @SneakyThrows
    @OperationLog(function = OperationLogFunction.DATA_FLOW, action = OperationLogAction.UPDATE,
            id = "#dataFlowBaseUpdateRequest.id")
    @Override
    public Boolean update(DataFlowUpdateRequest dataFlowUpdateRequest) {
        // 排除掉自己，检查名称是否存在
        if (this.lambdaQuery().eq(DataFlow::getName, dataFlowUpdateRequest.getName())
                .ne(DataFlow::getId, dataFlowUpdateRequest.getId())
                .eq(DataFlow::getWorkspaceCode, Context.getWorkspace().getCode())
                .exists()) {
            throw new ApiException("数据流名称已经存在");
        }
        DataFlow dataFlow = this.getById(dataFlowUpdateRequest.getId());
        if (dataFlow == null) {
            throw new ApiException("数据流不存在");
        }
        String designString = dataFlowUpdateRequest.getDesign();
        // 更新版本
        if (StrUtil.isBlank(dataFlow.getCurrentVersion())) {
            dataFlow.setCurrentVersion(VersionUtils.INIT_VERSION);
        } else {
            // 如果已经发布过，开始更新版本号
            if (StrUtil.isNotBlank(dataFlow.getPublishVersion())
                    // 并且存在数据流信息更新
                    && StrUtil.isNotBlank(designString)
            ) {
                // 如果测试与已经发布版本一致,则需要更新一个版本号
                if (dataFlow.getCurrentVersion().equals(dataFlow.getPublishVersion())) {
                    // 获取下一个版本
                    dataFlow.setCurrentVersion(VersionUtils.getNextVersion(dataFlow.getCurrentVersion()));
                } else {
                    // 更新小版本
                    dataFlow.setCurrentVersion(VersionUtils.getNextVersion(dataFlow.getCurrentVersion(), true));
                }
            }
        }
        this.orikaMapper.map(dataFlowUpdateRequest, dataFlow);
        List<String> specifyInstances = dataFlowUpdateRequest.getSpecifyInstances();
        if (specifyInstances != null) {
            dataFlow.setSpecifyInstances(JSON.toJSONString(specifyInstances));
        }
        if (StrUtil.isNotBlank(designString)) {
            // 存储引用关系
            List<String> datasourceCodes = JsonPath.read(designString, JSON_PATH_DATASOURCE_CODE);
            // 过滤掉空值
            datasourceCodes = datasourceCodes.stream().filter(StrUtil::isNotBlank).toList();
            // 去重复
            Set<String> datasourceCodeSet = new HashSet<>(datasourceCodes);
            dataFlow.setDatasourceCodes(JSON.toJSONString(datasourceCodeSet));
            if (CollUtil.isNotEmpty(datasourceCodeSet)) {
                // 找到不存在的数据源编码
                List<DataSource> dataSources = this.dataSourceService.lambdaQuery()
                        .select(DataSource::getStatus, DataSource::getCode)
                        .eq(DataSource::getWorkspaceCode, dataFlow.getWorkspaceCode())
                        .in(DataSource::getCode, datasourceCodeSet)
                        .list();
                // 数量是否一致
                if (datasourceCodeSet.size() != dataSources.size()) {
                    // 找到不存在的数据源编码
                    List<String> codes = dataSources.stream().map(DataSource::getCode).toList();
                    codes.forEach(datasourceCodeSet::remove);
                    throw new ApiException("数据源不存在:" + String.join(",", datasourceCodeSet));
                }
                // 状态是否禁用
                List<String> disableCodes = dataSources.stream()
                        .filter(dataSource -> !Objects.equals(dataSource.getStatus(), Status.ENABLE.name()))
                        .map(DataSource::getCode).toList();
                if (CollUtil.isNotEmpty(disableCodes)) {
                    throw new ApiException("数据源状态禁用:" + String.join(",", disableCodes));
                }
            }
            // 如果不是暂存，则校验合理性
            if (Objects.equals(dataFlowUpdateRequest.getTemporarily(), false)) {
                Design design = JSON.parseObject(designString, Design.class);
                if (CollUtil.isEmpty(design.getEdges())) {
                    throw new ApiException("数据流连线不能为空，必须存在两个组件及以上。");
                }
                List<Design.Edge> edges = design.getEdges();
                // 分组
                // SourceNodeId 对应多个 targetNodeId
                Map<String, List<String>> edgesGroup = edges.stream().collect(Collectors.groupingBy(
                        Design.Edge::getSourceNodeId,
                        Collectors.mapping(Design.Edge::getTargetNodeId, Collectors.toList())));
                for (Design.Node node : design.getNodes()) {
                    Design.Node.Properties properties = node.getProperties();
                    if (properties == null) {
                        continue;
                    }
                    List<String> targetNodeIds = edgesGroup.get(node.getId());
                    // 基本参数校验
                    ValidationUtils.validate(properties);
                    // 如果是job，校验cron表达式合法性
                    switch (properties) {
                        case Design.Node.Job jobBody -> {
                            if (!CronUtils.isValid(jobBody.getCron())) {
                                throw new ApiException("触发时间表达式不合法");
                            }
                            // 如果是job，必须有下游节点
                            if (CollUtil.isEmpty(targetNodeIds)) {
                                throw new ApiException("Job节点必须有下游节点");
                            }
                        }
                        case Design.Node.KafkaReceive ignored -> {
                            // 如果接收完消息，不做任何事情，则流程不合理
                            if (CollUtil.isEmpty(targetNodeIds)) {
                                throw new ApiException("Kafka接收完消息后，必须有下游节点");
                            }
                        }
                        case Design.Node.QueryDoris ignored -> {
                            // 如果是doris查询，必须有下游节点
                            if (CollUtil.isEmpty(targetNodeIds)) {
                                throw new ApiException("Doris查询完数据后，必须有下游节点");
                            }
                        }
                        case Design.Node.QueryMySQL ignored -> {
                            // 如果是mysql查询，必须有下游节点
                            if (CollUtil.isEmpty(targetNodeIds)) {
                                throw new ApiException("MySQL查询完数据后，必须有下游节点");
                            }
                        }
                        case Design.Node.Debezium ignored -> {
                            // 如果是debezium，必须有下游节点
                            if (CollUtil.isEmpty(targetNodeIds)) {
                                throw new ApiException("Debezium监听到数据后，必须有下游节点");
                            }
                        }
                        default -> {
                        }
                    }
                }
            }
            dataFlow.setDesign(designString);
        }
        return this.updateById(dataFlow);
    }

    /**
     * 获取数据流详情
     *
     * @param id d
     * @return r
     */
    @Override
    public DataFlowDetailResponse detail(Long id) {
        DataFlow dataFlow = this.getById(id);
        if (dataFlow == null) {
            return null;
        }
        DataFlowDetailResponse dataFlowDetailResponse = new DataFlowDetailResponse();
        this.orikaMapper.map(dataFlow, dataFlowDetailResponse);
        dataFlowDetailResponse.setDesign(JSON.parseObject(dataFlow.getDesign()));
        String specifyInstances = dataFlow.getSpecifyInstances();
        if (StrUtil.isNotBlank(specifyInstances)) {
            dataFlowDetailResponse.setSpecifyInstances(JSON.parseArray(specifyInstances, String.class));
        } else {
            dataFlowDetailResponse.setSpecifyInstances(Collections.emptyList());
        }
        return dataFlowDetailResponse;
    }

    /**
     * 发布
     *
     * @param publishRequest d
     * @return r
     */
    @OperationLog(function = OperationLogFunction.DATA_FLOW, action = OperationLogAction.PUBLISH,
            id = "#publishRequest.id")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean publish(PublishRequest publishRequest) {
        Long id = publishRequest.getId();
        DataFlow dataFlow = this.getById(id);
        if (dataFlow == null) {
            return false;
        }
        // 判断是否有可用数据流集群
        Collection<Server> flowServers = this.serverManager.availableList(ServerConstant.FLOW_SERVER);
        if (CollUtil.isEmpty(flowServers)) {
            // 服务都不可用
            throw new ApiException("没有可用数据流服务,暂时不可发布!");
        }
        // 如果已经发布版本与当前版本一致
        if (Objects.equals(dataFlow.getPublishVersion(), dataFlow.getCurrentVersion())) {
            // 如果已经是启用的，且版本号相同
            throw new ApiException("当前版本已经发布");
        }
        dataFlow.setStatus(Status.ENABLE.name());
        String flowDesign = dataFlow.getDesign();
        Design design = JSON.parseObject(flowDesign, Design.class);
        if (design == null) {
            throw new ApiException("数据流设计不能为空");
        }
        if (CollUtil.isEmpty(design.getNodes())) {
            throw new ApiException("数据流节点不能为空");
        }
        if (CollUtil.isEmpty(design.getEdges())) {
            throw new ApiException("数据流连线不能为空");
        }
        // 原来的版本变为禁用状态
        this.dataFlowPublishService.lambdaUpdate()
                .set(DataFlowPublish::getStatus, FlowStatus.HISTORY.name())
                .eq(DataFlowPublish::getCode, dataFlow.getCode())
                .in(DataFlowPublish::getStatus, Arrays.asList(FlowStatus.ENABLE.name(), FlowStatus.PAUSE.name()))
                .update();
        // 生成新的发布版本
        DataFlowPublish dataFlowPublish = new DataFlowPublish();
        this.orikaMapper.map(dataFlow, dataFlowPublish);
        dataFlowPublish.setId(null);
        // 重新填充当前时间
        dataFlowPublish.setCreateTime(null);
        dataFlowPublish.setUpdateTime(null);
        dataFlowPublish.setVersion(dataFlow.getCurrentVersion());
        dataFlowPublish.setCreateUserId(Context.getUser().getId());
        dataFlowPublish.setPublishDescription(publishRequest.getPublishDescription());
        this.dataFlowPublishService.save(dataFlowPublish);
        dataFlow.setPublishVersion(dataFlow.getCurrentVersion());
        this.updateById(dataFlow);
        // 清理员版本的异常信息
        RList<FlowError> flowErrors = this.redissonClient.getList(
                RedisKey.FLOW_ERROR.build(dataFlowPublish.getWorkspaceCode() + "-" + dataFlowPublish.getCode()));
        flowErrors.delete();
        DataFlowMessageBody dataFlowMessageBody = new DataFlowMessageBody();
        dataFlowMessageBody.setType(DataFlowMessageBody.Type.LOAD);
        dataFlowMessageBody.setId(dataFlowPublish.getId());
        dataFlowMessageBody.setWorkspaceCode(dataFlowPublish.getWorkspaceCode());
        this.applicationEventPublisher.publishEvent(new DataFlowEvent(dataFlowMessageBody));
        return true;
    }

    /**
     * 启动流程
     *
     * @param id d
     * @return r
     */
    @OperationLog(function = OperationLogFunction.DATA_FLOW, action = OperationLogAction.START,
            id = "#id")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean start(Long id) {
        DataFlow dataFlow = this.getById(id);
        if (dataFlow == null) {
            throw new ApiException("数据流不存在");
        }
        // 如果已经是启用状态
        if (StrUtil.equals(dataFlow.getStatus(), Status.ENABLE.name())) {
            throw new ApiException("数据流已经启用");
        }
        // 如果没有发布过
        if (StrUtil.isBlank(dataFlow.getPublishVersion())) {
            throw new ApiException("未发布的数据流不能启用");
        }
        // 启动
        dataFlow.setStatus(Status.ENABLE.name());
        this.updateById(dataFlow);
        // 查询到停用的发布版本，直接启用
        DataFlowPublish dataFlowPublish = this.dataFlowPublishService.lambdaQuery()
                .eq(DataFlowPublish::getWorkspaceCode, dataFlow.getWorkspaceCode())
                .eq(DataFlowPublish::getCode, dataFlow.getCode())
                .eq(DataFlowPublish::getVersion, dataFlow.getPublishVersion())
                .one();
        if (dataFlowPublish == null) {
            throw new ApiException("已发布的版本不存在");
        }
        // 如果已经是启动状态的
        if (StrUtil.equals(dataFlowPublish.getStatus(), Status.ENABLE.name())) {
            throw new ApiException("已发布数据流已经启用");
        }
        dataFlowPublish.setStatus(Status.ENABLE.name());
        this.dataFlowPublishService.updateById(dataFlowPublish);
        // 先清理之前的异常信息
        RList<FlowError> flowErrors = this.redissonClient.getList(RedisKey.FLOW_ERROR.build(
                dataFlowPublish.getWorkspaceCode() + "-" + dataFlowPublish.getCode())
        );
        flowErrors.delete();
        // 发送给数据流集群
        DataFlowMessageBody dataFlowMessageBody = new DataFlowMessageBody();
        dataFlowMessageBody.setType(DataFlowMessageBody.Type.LOAD);
        dataFlowMessageBody.setId(dataFlowPublish.getId());
        dataFlowMessageBody.setWorkspaceCode(dataFlowPublish.getWorkspaceCode());
        this.applicationEventPublisher.publishEvent(new DataFlowEvent(dataFlowMessageBody));
        return true;
    }

    /**
     * 停止流程
     *
     * @param id d
     * @return r
     */
    @OperationLog(function = OperationLogFunction.DATA_FLOW, action = OperationLogAction.STOP,
            id = "#id")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean stop(Long id) {
        DataFlow dataFlow = this.getById(id);
        if (dataFlow == null) {
            return false;
        }
        dataFlow.setStatus(FlowStatus.PAUSE.name());
        this.updateById(dataFlow);
        // 已发布也需要禁止
        DataFlowPublish dataFlowPublish = this.dataFlowPublishService.lambdaQuery()
                .eq(DataFlowPublish::getCode, dataFlow.getCode())
                .eq(DataFlowPublish::getStatus, FlowStatus.ENABLE.name())
                .one();
        if (dataFlowPublish != null) {
            // 改为禁用
            this.dataFlowPublishService.lambdaUpdate()
                    .set(DataFlowPublish::getStatus, FlowStatus.PAUSE.name())
                    .eq(DataFlowPublish::getId, dataFlowPublish.getId())
                    .update();
            // 清理异常信息
            RList<FlowError> flowErrors = this.redissonClient.getList(RedisKey.FLOW_ERROR.build(
                    dataFlowPublish.getWorkspaceCode() + "-" + dataFlowPublish.getCode()));
            flowErrors.delete();
            // 通知启动数据流
            DataFlowMessageBody dataFlowMessageBody = new DataFlowMessageBody();
            dataFlowMessageBody.setId(dataFlowPublish.getId());
            dataFlowMessageBody.setType(DataFlowMessageBody.Type.REMOVE);
            dataFlowMessageBody.setWorkspaceCode(dataFlowPublish.getWorkspaceCode());
            this.applicationEventPublisher.publishEvent(new DataFlowEvent(dataFlowMessageBody));
        }
        return true;
    }

    /**
     * 删除流程
     *
     * @param id d
     * @return r
     */
    @OperationLog(function = OperationLogFunction.DATA_FLOW, action = OperationLogAction.DELETE,
            id = "#id")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean delete(Long id) {
        DataFlow dataFlow = this.getById(id);
        if (dataFlow == null) {
            return false;
        }
        // 如果运行中的，二次确认需要先停用，才能删除
        if (StrUtil.equals(dataFlow.getStatus(), FlowStatus.ENABLE.name())) {
            throw new ApiException("数据流正在运行中，请先停止数据流");
            // 以下通知逻辑暂时保留
        }
        this.removeById(id);
        // 如果已经发布 通知查询服务删除
        if (StrUtil.equals(dataFlow.getStatus(), FlowStatus.ENABLE.name())) {
            DataFlowPublish dataFlowPublish = this.dataFlowPublishService.lambdaQuery()
                    .eq(DataFlowPublish::getCode, dataFlow.getCode())
                    .eq(DataFlowPublish::getStatus, FlowStatus.ENABLE.name())
                    .one();
            if (dataFlowPublish != null) {
                // 清理异常信息
                RList<FlowError> flowErrors = this.redissonClient.getList(RedisKey.FLOW_ERROR.build(
                        dataFlowPublish.getWorkspaceCode() + "-" + dataFlowPublish.getCode()));
                flowErrors.delete();
                // 改为禁用
                this.dataFlowPublishService.lambdaUpdate()
                        .set(DataFlowPublish::getStatus, FlowStatus.HISTORY.name())
                        .eq(DataFlowPublish::getId, dataFlowPublish.getId())
                        .update();
                DataFlowMessageBody dataFlowMessageBody = new DataFlowMessageBody();
                dataFlowMessageBody.setId(dataFlowPublish.getId());
                dataFlowMessageBody.setType(DataFlowMessageBody.Type.REMOVE);
                dataFlowMessageBody.setWorkspaceCode(dataFlowPublish.getWorkspaceCode());
                this.applicationEventPublisher.publishEvent(new DataFlowEvent(dataFlowMessageBody));
            }
        }
        return true;
    }

    /**
     * 回滚至某个版本
     *
     * @param id id
     * @return r
     */
    @OperationLog(function = OperationLogFunction.DATA_FLOW, action = OperationLogAction.ROLLBACK,
            id = "#id")
    @Override
    public Boolean rollback(Long id) {
        final DataFlowPublish dataFlowPublish = this.dataFlowPublishService.getById(id);
        if (dataFlowPublish == null) {
            throw new ApiException("回滚的版本不存在");
        }
        DataFlow dataFlow = this.lambdaQuery()
                .eq(DataFlow::getCode, dataFlowPublish.getCode())
                .eq(DataFlow::getWorkspaceCode, dataFlowPublish.getWorkspaceCode())
                .one();
        if (dataFlow == null) {
            return false;
        }
        Long flowId = dataFlow.getId();
        String status = dataFlow.getStatus();
        String currentVersion = dataFlow.getCurrentVersion();
        String publishVersion = dataFlow.getPublishVersion();
        this.orikaMapper.map(dataFlowPublish, dataFlow);
        // 上方复制，导致ID错乱
        dataFlow.setId(flowId);
        dataFlow.setStatus(status);
        if (currentVersion.equals(publishVersion)) {
            // 如果没有待发布，生成一个大版本
            dataFlow.setCurrentVersion(VersionUtils.getNextVersion(currentVersion));
        } else {
            // 当前已经是待发布，版本保留
            dataFlow.setCurrentVersion(currentVersion);
        }
        return this.updateById(dataFlow);
    }


    /**
     * * 查询已经发布过的数据流 编码还有名称
     *
     * @return r
     */
    @Override
    public List<ListPublishedResponse> listPublished(String query) {
        WorkspaceData workspace = Context.getWorkspace();
        List<DataFlow> list = this.lambdaQuery()
                .select(DataFlow::getId, DataFlow::getCode, DataFlow::getPublishVersion, DataFlow::getName)
                .eq(DataFlow::getWorkspaceCode, workspace.getCode())
                // code 或者name模糊搜索
                .and(StrUtil.isNotBlank(query), m -> m.like(DataFlow::getCode, query)
                        .or().like(DataFlow::getName, query))
                .isNotNull(DataFlow::getPublishVersion)
                .last("ORDER BY CASE status " +
                        "WHEN 'ENABLE' THEN 1 " +
                        "WHEN 'PAUSE' THEN 2 " +
                        "WHEN 'HISTORY' THEN 3 " +
                        "ELSE 4 END ASC, update_time DESC")
                .last("limit 20")
                .list();
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // publish
        List<String> codes = list.stream().map(DataFlow::getCode).toList();
        List<DataFlowPublish> dataFlowPublishes = this.dataFlowPublishService.lambdaQuery()
                .eq(DataFlowPublish::getWorkspaceCode, workspace.getCode())
                .in(DataFlowPublish::getCode, codes)
                .in(DataFlowPublish::getStatus, Arrays.asList(FlowStatus.ENABLE.name(), FlowStatus.PAUSE.name()))
                .list();
        // 按照code+版本to map
        Map<String, DataFlowPublish> dataFlowPublishMap = dataFlowPublishes.stream()
                .collect(Collectors.toMap(m -> String.format("%s-%s", m.getCode(), m.getVersion()), Function.identity()));
        return list.stream().map(m -> {
            ListPublishedResponse listPublishedResponse = new ListPublishedResponse();
            this.orikaMapper.map(m, listPublishedResponse);
            // 获取到线上跑的版本的组件信息
            DataFlowPublish dataFlowPublish = dataFlowPublishMap.get(String.format("%s-%s", m.getCode(), m.getPublishVersion()));
            if (dataFlowPublish != null) {
                Design design = JSON.parseObject(dataFlowPublish.getDesign(), Design.class);
                List<Design.Node> nodes = design.getNodes();
                List<ListPublishedResponse.Component> components = nodes.stream()
                        .map(n -> {
                            ListPublishedResponse.Component component = new ListPublishedResponse.Component();
                            component.setId(n.getId());
                            component.setType(n.getType());
                            component.setName(n.getProperties().getName());
                            return component;
                        }).collect(Collectors.toList());
                listPublishedResponse.setComponents(components);
            }
            return listPublishedResponse;
        }).collect(Collectors.toList());
    }


}

