package cn.dataplatform.open.common.server;

import cn.dataplatform.open.common.alarm.scene.ServiceOfflineNoticeScene;
import cn.dataplatform.open.common.alarm.scene.ServiceOnlineNoticeScene;
import cn.dataplatform.open.common.body.AlarmSceneMessageBody;
import cn.dataplatform.open.common.component.JsonJacksonCodec;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.enums.RedisKey;
import cn.dataplatform.open.common.event.AlarmSceneEvent;
import cn.dataplatform.open.common.util.IPUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * DependsOn({"redisson", "eventPublisherListener"}) 解决优雅停机时此组件被提前关闭，例如 redisson is shutdown
 * o.s.beans.factory.support.DisposableBeanAdapter -[]- Invocation of destroy method failed on bean with name 'serverManager': org.springframework.amqp.AmqpApplicationContextClosedException:
 * The ApplicationContext is closed and the ConnectionFactory can no longer create connections.
 *
 * @author dingqianwen
 * @date 2025/1/26
 * @since 1.0.0
 */
@DependsOn({"redisson", "eventPublisherListener"})
@Order(0)
@Slf4j
@Component
public class ServerManager implements ApplicationListener<ServletWebServerInitializedEvent>, DisposableBean {

    /**
     * 启动时间
     */
    @Getter
    public LocalDateTime startTime;
    /**
     * 销毁时,先销毁此线程池,防止还在一直注册
     */
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();


    @Value("${server.port:}")
    private Integer port;
    @Value("${spring.application.name:unknown}")
    @Getter
    private String applicationName;

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private ServerMonitor serverMonitor;

    /**
     * 服务注册
     *
     * @param event 事件
     */
    @Override
    public void onApplicationEvent(@NonNull ServletWebServerInitializedEvent event) {
        String requestId = UUID.randomUUID().toString(true);
        try {
            MDC.put(Constant.REQUEST_ID, requestId);
            log.info("服务启动成功,开始注册服务信息");
            // 服务启动通知
            AlarmSceneMessageBody alarmSceneMessageBody = new AlarmSceneMessageBody(new ServiceOnlineNoticeScene());
            this.applicationEventPublisher.publishEvent(new AlarmSceneEvent(alarmSceneMessageBody));
        } finally {
            MDC.remove(Constant.REQUEST_ID);
        }
        String hostAddress = IPUtils.SERVER_IP;
        String instanceId = hostAddress + ":" + this.port;
        RMapCache<String, Server> mapCache = this.redissonClient.getMapCache(RedisKey.SERVERS.build(this.applicationName)
                , new JsonJacksonCodec());
        // 注册服务实例，并定期心跳
        this.scheduledExecutorService.scheduleAtFixedRate(() -> {
            MDC.put(Constant.REQUEST_ID, requestId);
            try {
                Server server = mapCache.get(instanceId);
                // 如果之前没有注册过
                if (server == null) {
                    server = new Server();
                    server.setHost(hostAddress);
                    server.setPort(this.port);
                    server.setFastHeartbeat(LocalDateTime.now());
                } else {
                    server.setStatus(null);
                }
                if (this.startTime == null) {
                    this.startTime = LocalDateTime.now();
                    // 当前服务启动时间
                    server.setLatelyStartTime(startTime);
                }
                // bug修复,运行期间,手动删除redis数据
                if (server.getLatelyStartTime() == null) {
                    server.setLatelyStartTime(this.startTime);
                }
                server.setLastHeartbeat(LocalDateTime.now());
                // 上报内存和cpu使用率，方便后续做负载均衡调度
                BigDecimal jvmCpuUsage = this.serverMonitor.getJvmCpuUsage();
                // 第一次获取可能为空，再次获取一次
                if (jvmCpuUsage.compareTo(BigDecimal.ZERO) == 0) {
                    jvmCpuUsage = this.serverMonitor.getJvmCpuUsage();
                }
                server.setTotalMemory(this.serverMonitor.getJvmTotalMemory());
                server.setFreeMemory(this.serverMonitor.getJvmFreeMemory());
                server.setCpuUsageRatio(jvmCpuUsage);
                // 过期时间默认一周
                mapCache.put(instanceId, server, 24 * 7, TimeUnit.HOURS);
                log.debug("服务实例心跳 : {}", instanceId);
            } catch (Exception e) {
                log.error("服务实例心跳异常", e);
            } finally {
                MDC.clear();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * 主动销毁
     */
    @Override
    public void destroy() {
        String requestId = UUID.randomUUID().toString(true);
        try {
            MDC.put(Constant.REQUEST_ID, requestId);
            log.info("服务即将关闭,开始服务实例注销");
            this.scheduledExecutorService.shutdown();
            String instanceId = this.instanceId();
            RMapCache<Object, Server> mapCache = this.redissonClient.getMapCache(RedisKey.SERVERS.build(this.applicationName)
                    , new JsonJacksonCodec());
            Server server = mapCache.get(instanceId);
            server.setStatus(ServerStatus.OFFLINE);
            mapCache.put(instanceId, server);
            log.info("服务实例注销成功,服务实例ID:{}", instanceId);
            // 发送服务下线告警
            AlarmSceneMessageBody alarmSceneMessageBody = new AlarmSceneMessageBody(new ServiceOfflineNoticeScene());
            this.applicationEventPublisher.publishEvent(new AlarmSceneEvent(alarmSceneMessageBody));
        } finally {
            MDC.remove(Constant.REQUEST_ID);
        }
    }

    /**
     * 获取服务实例ID
     */
    public String instanceId() {
        return IPUtils.SERVER_IP + ":" + port;
    }

    /**
     * 查询当前服务是否正常
     *
     * @return 如果服务正常（状态为 ONLINE）,返回 true；否则返回 false
     */
    public Boolean status() {
        String instanceId = this.instanceId();
        return this.status(instanceId);
    }

    /**
     * 查询某个服务是否正常
     *
     * @param instanceId 服务实例ID
     * @return 如果服务正常（状态为 ONLINE）,返回 true；否则返回 false
     */
    public Boolean status(String instanceId) {
        RMapCache<String, Server> mapCache = this.redissonClient.getMapCache(RedisKey.SERVERS.build(this.applicationName)
                , new JsonJacksonCodec());
        Server server = mapCache.get(instanceId);
        if (server == null) {
            log.warn("服务 {} 未找到", instanceId);
            return false;
        }
        if (server.getStatus() == ServerStatus.ONLINE) {
            return true;
        }
        log.warn("服务 {} 状态异常", instanceId);
        return false;
    }

    /**
     * 获取服务节点信息
     *
     * @return 节点信息
     */
    public Collection<Server> list() {
        return this.list(this.applicationName);
    }

    /**
     * 获取服务节点信息
     *
     * @param serverName 服务名称
     * @return 节点信息
     */
    public Collection<Server> list(String serverName) {
        RMapCache<String, Server> mapCache = this.redissonClient.getMapCache(RedisKey.SERVERS.build(serverName)
                , new JsonJacksonCodec());
        if (CollUtil.isEmpty(mapCache)) {
            return Collections.emptyList();
        }
        return mapCache.values();
    }

    /**
     * 获取在线服务节点信息
     *
     * @return 节点信息
     */
    public Collection<Server> availableList(String serverName) {
        Collection<Server> list = this.list(serverName);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().filter(s -> s.getStatus() == ServerStatus.ONLINE).toList();
    }

}
