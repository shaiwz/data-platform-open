package cn.dataplatform.open.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/19
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum RedisKey {

    /**
     * 数据流启动、运行异常标记
     */
    FLOW_ERROR("dp:flow:error:", "数据流启动、运行异常标记"),
    /**
     * 数据流心跳
     */
    FLOW_HEARTBEAT("dp:flow:heartbeat:", "数据流心跳"),
    /**
     * 数据流任务锁
     */
    FLOW_JOB_LOCK("dp:flow:job:lock:", "数据流任务锁"),
    /**
     * 数据流Debezium执行锁
     */
    FLOW_DEBEZIUM_LOCK("dp:flow:debezium:lock:", "数据流Debezium执行锁"),
    /**
     * debezium心跳
     */
    FLOW_DEBEZIUM_HEARTBEAT("dp:flow:debezium:heartbeat:", "数据流Debezium心跳"),
    /**
     * "dp:flow:dispatch:leader:lock"
     */
    FLOW_DISPATCH_LEADER_LOCK("dp:flow:dispatch:leader:lock", "数据流调度选举锁"),
    /**
     * 如果数据流最近几分钟已经调度过，则不用重新调度，等待启动中，防止重复调度
     */
    FLOW_DISPATCH_LOCK("dp:flow:dispatch:lock:", "数据流调度锁"),
    /**
     * 限制每次需要单个节点执行的组件
     */
    FLOW_COMPONENT_ONLY("dp:flow:component:only:", "数据流单节点执行的组件"),
    /**
     * 数据流限流组件使用
     */
    FLOW_RATE_LIMIT("dp:flow:rate-limit:", "数据流限流组件使用"),
    /**
     * 防止启动过程中过慢无心跳导致重复调度
     */
    FLOW_COMPONENT_MESSAGE_LOCK("dp:flow:component:message:lock:", "数据流组件消息锁"),

    /**
     * 登录用户Token
     */
    TOKEN("dp:token:", "登录用户Token"),

    /**
     * 维护用户id与token的关系，用于根据用户ID查询对应的Token信息
     */
    USER_TOKEN(" dp:user:token", "维护用户ID与Token的关系"),

    /**
     * 服务注册列表
     */
    SERVERS("dp:servers:", "服务注册"),

    /**
     * 告警机器人轮询点位
     */
    ALARM_ROBOT_POLLING("dp:alarm:robot:polling:", "告警机器人轮询"),
    /**
     * 数据对齐任务锁
     */
    ALIGN_JOB_LOCK("dp:align:job:lock:", "数据对齐任务锁"),

    /**
     * 查询模板缓存
     */
    QUERY_TEMPLATE_CACHE("dp:query-template:cache:", "查询模板缓存"),
    /**
     * 查询模板限流
     */
    QUERY_TEMPLATE_LIMIT("dp:query-template:limit:", "查询模板限流"),
    /**
     * (@Scheduled)定时任务全局锁
     */
    SCHEDULED_LOCK("dp:scheduled:lock:", "定时任务全局锁"),
    /**
     * 接口限流
     */
    RATE_LIMIT("dp:rate-limit:", "接口限流"),
    /**
     * 防止重复提交使用
     */
    RESUBMIT_LOCK("dp:resubmit-lock:", "防重复提交锁");

    private final String key;
    private final String desc;

    /**
     * 获取key
     *
     * @return key
     */
    public String build(String suffix) {
        Objects.requireNonNull(suffix, "suffix must not be null");
        return key + suffix;
    }

}
