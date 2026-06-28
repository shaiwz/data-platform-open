/*
 * ============================================================================
 *
 *                    数海文舟 (DATA PLATFORM) 版权所有 © 2025
 *
 *       本软件受著作权法和国际版权条约保护，本软件受著作权法和国际版权条约保护，未经明确书面授权，任何单位或个人不得对本软件进行复制、修改、分发、
 *       逆向工程、商业用途等任何形式的非法使用。违者将面临人民币100万元的
 *       法定罚款及可能的法律追责。
 *
 *       举报侵权行为可获得实际罚款金额40%的现金奖励。
 *       举报渠道：
 *           - 法务邮箱：dingqw@shaiwz.com，761945125@qq.com
 *
 *       COPYRIGHT (C) 2025 dingqianwen COMPANY. ALL RIGHTS RESERVED.
 *
 * ============================================================================
 */
package cn.dataplatform.open.support.service.alarm.impl;

import cn.dataplatform.open.common.util.MDCUtils;
import cn.dataplatform.open.support.service.alarm.robot.DingTalkRobot;
import cn.dataplatform.open.support.service.alarm.robot.LarkRobot;
import cn.dataplatform.open.support.service.alarm.robot.Robot;
import cn.dataplatform.open.support.service.alarm.robot.WeComRobot;
import cn.dataplatform.open.support.service.alarm.robot.content.Content;
import cn.dataplatform.open.support.service.alarm.robot.content.LarkContent;
import cn.dataplatform.open.support.service.alarm.robot.content.TextContent;
import cn.dataplatform.open.common.body.AlarmMessageBody;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.constant.DateConstant;
import cn.dataplatform.open.common.enums.RedisKey;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.enums.alarm.AlarmLogStatus;
import cn.dataplatform.open.common.enums.alarm.AlarmRobotCategory;
import cn.dataplatform.open.common.enums.alarm.AlarmRobotDispatchStrategy;
import cn.dataplatform.open.common.util.ParallelStreamUtils;
import cn.dataplatform.open.common.vo.alarm.robot.Receive;
import cn.dataplatform.open.common.vo.alarm.robot.Silent;
import cn.dataplatform.open.support.excepiton.AlarmSilentException;
import cn.dataplatform.open.support.config.ThreadPoolConfig;
import cn.dataplatform.open.support.service.alarm.AlarmService;
import cn.dataplatform.open.support.store.entity.AlarmLog;
import cn.dataplatform.open.support.store.entity.AlarmRobot;
import cn.dataplatform.open.support.store.entity.AlarmScene;
import cn.dataplatform.open.support.store.entity.AlarmTemplate;
import cn.dataplatform.open.support.store.mapper.AlarmLogMapper;
import cn.dataplatform.open.support.store.mapper.AlarmRobotMapper;
import cn.dataplatform.open.support.store.mapper.AlarmTemplateMapper;
import cn.dataplatform.open.support.util.FreeMarkerUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.PropertyPreFilter;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/22
 * @since 1.0.0
 */
@Slf4j
@Service
public class AlarmServiceImpl implements AlarmService {

    /**
     * 内置模板参数
     */
    public static final String WORKSPACE_CODE = Constant.WORKSPACE_CODE;
    public static final String REQUEST_ID = Constant.REQUEST_ID;
    public static final String SERVER_NAME = "serverName";
    public static final String INSTANCE_ID = "instanceId";
    public static final String ALARM_TIME = "alarmTime";
    public static final String SCENE_CODE = "sceneCode";
    public static final String SCENE_NAME = "sceneName";
    public static final String TEMPLATE_CODE = "templateCode";
    public static final String ROBOT_CODE = "robotCode";
    /**
     * 调试用,可以打印出所有的参数
     */
    public static final String DEBUG = "debug";
    /**
     * 序列化时跳过 debug 字段
     */
    private static final PropertyPreFilter NO_DEBUG_FILTER = (writer, object, name)
            -> !DEBUG.equals(name);

    @Resource
    private AlarmRobotMapper alarmRobotMapper;
    @Resource
    private AlarmTemplateMapper alarmTemplateMapper;
    @Resource
    private AlarmLogMapper alarmLogMapper;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 告警
     *
     * @param alarmMessageBody 告警消息
     */
    @Override
    public void alarm(AlarmMessageBody alarmMessageBody) {
        this.alarm(alarmMessageBody, null);
    }

    /**
     * 异步执行告警
     *
     * @param alarmMessageBody 告警消息
     * @param alarmScene       告警场景
     */
    @Async(ThreadPoolConfig.VIRTUAL_EXECUTOR)
    @Override
    public void alarmAsync(AlarmMessageBody alarmMessageBody, AlarmScene alarmScene) {
        this.alarm(alarmMessageBody, alarmScene);
    }

    /**
     * 告警
     *
     * @param alarmMessageBody 告警消息
     * @param alarmScene       告警场景
     */
    @Override
    public void alarm(AlarmMessageBody alarmMessageBody, AlarmScene alarmScene) {
        String workspaceCode = alarmMessageBody.getWorkspaceCode();
        String robotCode = alarmMessageBody.getRobotCode();
        AlarmRobot alarmRobot = this.alarmRobotMapper.selectOne(Wrappers.<AlarmRobot>lambdaQuery()
                .eq(AlarmRobot::getWorkspaceCode, workspaceCode)
                .eq(AlarmRobot::getStatus, Status.ENABLE.name())
                .eq(AlarmRobot::getCode, robotCode));
        if (alarmRobot == null) {
            log.warn("机器人不存在或未启用, 告警消息被丢弃");
            return;
        }
        String requestId = MDCUtils.getRequestId();
        // 初始化内置请求参数-方便模板配置,以$开头
        this.mergeBuiltInParameters(alarmMessageBody, requestId, alarmScene);
        Status recordLogSwitch = Status.valueOf(alarmRobot.getRecordLogSwitch());
        AlarmLog alarmLog = null;
        // 是否需要记录日志
        if (recordLogSwitch.equals(Status.ENABLE)) {
            alarmLog = new AlarmLog();
            alarmLog.setRequestId(requestId);
            alarmLog.setSceneCode(alarmScene.getCode());
            alarmLog.setStatus(AlarmLogStatus.SENDING.name());
            alarmLog.setRobotCode(alarmMessageBody.getRobotCode());
            alarmLog.setTemplateCode(alarmMessageBody.getTemplateCode());
            alarmLog.setServerName(alarmMessageBody.getServerName());
            alarmLog.setInstanceId(alarmMessageBody.getInstanceId());
            alarmLog.setWorkspaceCode(alarmMessageBody.getWorkspaceCode());
            // 日志不带 debug 字段
            alarmLog.setParameter(JSON.toJSONString(alarmMessageBody.getParameter(), NO_DEBUG_FILTER));
            alarmLog.setCreateTime(alarmMessageBody.getAlarmTime());
            this.alarmLogMapper.insert(alarmLog);
        }
        try {
            this.doAlarm(alarmRobot, alarmMessageBody);
            log.info("发送告警消息成功");
            if (alarmLog != null) {
                alarmLog.setStatus(AlarmLogStatus.SUCCESS.name());
                this.alarmLogMapper.updateById(alarmLog);
            }
        } catch (AlarmSilentException ase) {
            if (alarmLog != null) {
                alarmLog.setStatus(AlarmLogStatus.SILENT.name());
                alarmLog.setErrorMessage(StrUtil.maxLength(ase.getMessage(), 2000));
                this.alarmLogMapper.updateById(alarmLog);
            }
        } catch (Exception e) {
            log.warn("发送告警消息失败", e);
            if (alarmLog != null) {
                alarmLog.setStatus(AlarmLogStatus.FAILED.name());
                alarmLog.setErrorMessage(ExceptionUtil.stacktraceToString(e, 2000));
                this.alarmLogMapper.updateById(alarmLog);
            }
        }
    }

    /**
     * 初始化内置请求参数-方便模板配置,以$开头
     *
     * @param alarmMessageBody 告警消息
     * @param requestId        请求ID
     * @param alarmScene       告警场景编码
     */
    private void mergeBuiltInParameters(AlarmMessageBody alarmMessageBody,
                                        String requestId, AlarmScene alarmScene) {
        Map<String, Object> parameter = alarmMessageBody.getParameter();
        if (!parameter.containsKey(REQUEST_ID)) {
            parameter.put(REQUEST_ID, requestId);
        }
        if (!parameter.containsKey(SERVER_NAME)) {
            parameter.put(SERVER_NAME, alarmMessageBody.getServerName());
        }
        if (!parameter.containsKey(INSTANCE_ID)) {
            parameter.put(INSTANCE_ID, alarmMessageBody.getInstanceId());
        }
        if (!parameter.containsKey(ALARM_TIME)) {
            parameter.put(ALARM_TIME, LocalDateTimeUtil.format(alarmMessageBody.getAlarmTime(), DateConstant.DATE_TIME_FORMATTER));
        }
        if (!parameter.containsKey(WORKSPACE_CODE)) {
            parameter.put(WORKSPACE_CODE, alarmMessageBody.getWorkspaceCode());
        }
        if (!parameter.containsKey(SCENE_CODE)) {
            parameter.put(SCENE_CODE, alarmScene.getCode());
        }
        if (!parameter.containsKey(SCENE_NAME)) {
            parameter.put(SCENE_NAME, alarmScene.getName());
        }
        // 使用的模板编码
        if (!parameter.containsKey(TEMPLATE_CODE)) {
            parameter.put(TEMPLATE_CODE, alarmMessageBody.getTemplateCode());
        }
        // 使用的机器人编码
        if (!parameter.containsKey(ROBOT_CODE)) {
            parameter.put(ROBOT_CODE, alarmMessageBody.getRobotCode());
        }
        // 调试用,可以打印出所有的参数,不允许被覆盖此参数
        parameter.put(DEBUG, JSON.toJSONString(parameter));
    }

    /**
     * 发送告警
     *
     * @param alarmRobot       机器人
     * @param alarmMessageBody 告警消息
     */
    @SneakyThrows
    private void doAlarm(AlarmRobot alarmRobot, AlarmMessageBody alarmMessageBody) {
        String workspaceCode = alarmMessageBody.getWorkspaceCode();
        String robotCode = alarmMessageBody.getRobotCode();
        String templateCode = alarmMessageBody.getTemplateCode();
        Map<String, Object> parameter = alarmMessageBody.getParameter();
        AlarmTemplate alarmTemplate = this.alarmTemplateMapper.selectOne(Wrappers.<AlarmTemplate>lambdaQuery()
                .eq(AlarmTemplate::getWorkspaceCode, workspaceCode)
                .eq(AlarmTemplate::getStatus, Status.ENABLE.name())
                .eq(AlarmTemplate::getCode, templateCode));
        if (alarmTemplate == null) {
            log.warn("模板不存在或未启用, 告警消息被丢弃");
            return;
        }
        String templateContent = alarmTemplate.getTemplateContent();
        // 模板套入参数
        if (StrUtil.isNotBlank(templateContent)) {
            // 使用FreeMarker模板引擎处理模板
            templateContent = FreeMarkerUtils.processTemplate(alarmTemplate.getCode(), templateContent, parameter);
        }
        String category = alarmRobot.getCategory();
        AlarmRobotCategory alarmRobotCategory = AlarmRobotCategory.valueOf(category);
        Content content;
        Robot robot = switch (alarmRobotCategory) {
            case LARK -> {
                if (StrUtil.isNotBlank(alarmTemplate.getExternalTemplateCode())) {
                    LarkContent larkContent = new LarkContent();
                    // 外部系统的模板编码,例如飞书的消息卡片 外部
                    larkContent.setTemplateId(alarmTemplate.getExternalTemplateCode());
                    // 外部消息模板参数
                    larkContent.setTemplateParameter(parameter);
                    content = larkContent;
                } else {
                    content = new TextContent(templateContent);
                }
                yield SpringUtil.getBean(LarkRobot.class);
            }
            case DING_TALK -> {
                content = new TextContent(templateContent);
                yield SpringUtil.getBean(DingTalkRobot.class);
            }
            case WE_COM -> {
                content = new TextContent(templateContent);
                yield SpringUtil.getBean(WeComRobot.class);
            }
            default -> throw new UnsupportedOperationException("不支持的机器人类型: " + category);
        };
        // 告警沉默判断
        List<Silent> silents = alarmRobot.getSilent();
        // 告警沉默判断
        this.assertNotSilent(content, silents, alarmMessageBody.getAlarmTime());
        List<Receive> receives = alarmRobot.getReceives();
        // 判断发送模式
        String dispatchStrategy = alarmRobot.getDispatchStrategy();
        if (Objects.equals(dispatchStrategy, AlarmRobotDispatchStrategy.BROADCAST.name())) {
            // 全部发送
            ParallelStreamUtils.forEach(receives, receive -> robot.send(receive.getKey(), content), false);
        } else if (Objects.equals(dispatchStrategy, AlarmRobotDispatchStrategy.POLLING.name())) {
            // 轮询发送
            RAtomicLong atomicLong = this.redissonClient.getAtomicLong(RedisKey.ALARM_ROBOT_POLLING.build(workspaceCode + robotCode));
            // 当前自增索引
            long index = atomicLong.incrementAndGet();
            // 总机器人数量
            int size = receives.size();
            // 获取当前要发送的机器人
            Receive receive = receives.get((int) (index % size));
            robot.send(receive.getKey(), content);
            // 如果index超出long最大值,重置
            if (index ==
                    // 提前重置
                    Long.MAX_VALUE - 10000) {
                // 告警不需要高精度轮询
                atomicLong.set(0);
            }
        } else if (Objects.equals(dispatchStrategy, AlarmRobotDispatchStrategy.RANDOM.name())) {
            // 随机发送
            Receive receive = receives.get((int) (Math.random() * receives.size()));
            robot.send(receive.getKey(), content);
        } else {
            throw new UnsupportedOperationException("不支持的发送模式: " + dispatchStrategy);
        }
    }

    /**
     * 告警沉默判断-命中则抛 {@link AlarmSilentException} 中止发送
     *
     * @param content   告警内容
     * @param silents   沉默规则，会就地剔除已过期项
     * @param alarmTime 告警时间
     */
    private void assertNotSilent(Content content, List<Silent> silents,
                                 LocalDateTime alarmTime) {
        if (CollUtil.isEmpty(silents)) {
            return;
        }
        // 过滤掉过期的规则
        silents.removeIf(silent -> silent.getExpire() != null && silent.getExpire()
                .isBefore(alarmTime));
        if (CollUtil.isEmpty(silents)) {
            return;
        }
        Map<String, String> keyMap = silents.stream().map(Silent::getKeys)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(k -> k, k -> k));
        AhoCorasickDoubleArrayTrie<String> trie = new AhoCorasickDoubleArrayTrie<>();
        trie.build(keyMap);
        Collection<AhoCorasickDoubleArrayTrie.Hit<String>> hits = trie.parseText(JSON.toJSONString(content));
        if (!hits.isEmpty()) {
            // 存在匹配的关键词,不发送消息；最多打印5个命中的关键词
            String jsonString = JSON.toJSONString(hits.stream()
                    .limit(5)
                    .map(m -> m.value)
                    .toList());
            log.info("告警消息被沉默, 告警消息中包含关键词: {}", jsonString);
            throw new AlarmSilentException(jsonString);
        }
    }

}
