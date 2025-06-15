package cn.dataplatform.open.web.listener;


import cn.dataplatform.open.common.alarm.scene.ServerNoticeMessageExceptionScene;
import cn.dataplatform.open.common.body.AlarmSceneMessageBody;
import cn.dataplatform.open.common.body.DataSourceMessageBody;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.event.AlarmSceneEvent;
import cn.dataplatform.open.web.config.RabbitConfig;
import cn.dataplatform.open.web.service.datasource.impl.DataSourceServiceImpl;
import cn.dataplatform.open.web.store.entity.DataSource;
import cn.hutool.cache.impl.TimedCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/11
 * @since 1.0.0
 */
@Slf4j
@Component
public class DataSourceMessageListener {

    @Resource
    private DataSourceServiceImpl dataSourceService;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 数据源消息
     *
     * @param messaging 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue,
            exchange = @Exchange(value = RabbitConfig.SOURCE_EXCHANGE, type = ExchangeTypes.FANOUT)
    ))
    public void onMessage(Message<DataSourceMessageBody> messaging) {
        String requestId = messaging.getHeaders().get(Constant.REQUEST_ID, String.class);
        MDC.put(Constant.REQUEST_ID, requestId);
        DataSourceMessageBody dataSourceMessageBody = messaging.getPayload();
        try {
            log.info("数据源消息:{}", dataSourceMessageBody);
            DataSource dataSource = this.dataSourceService.getById(dataSourceMessageBody.getId());
            if (dataSource == null) {
                log.warn("数据源不存在:{}", dataSourceMessageBody.getId());
                return;
            }
            String code = dataSource.getCode();
            DataSourceMessageBody.Type type = dataSourceMessageBody.getType();
            switch (type) {
                case UPDATE:
                case REMOVE:
                    TimedCache<String, Object> defaultDataSourceCache = this.dataSourceService.getDefaultDataSourceCache();
                    if (defaultDataSourceCache.isEmpty()) {
                        break;
                    }
                    // 遍历删除指定code:前缀的缓存数据
                    String cacheKeyPrefix = code + ":";
                    Set<String> cacheKeys = defaultDataSourceCache.keySet();
                    for (String cacheKey : cacheKeys) {
                        // 如果缓存键以指定前缀开头，则删除该缓存
                        if (cacheKey.startsWith(cacheKeyPrefix)) {
                            defaultDataSourceCache.remove(cacheKey);
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            ServerNoticeMessageExceptionScene scene = new ServerNoticeMessageExceptionScene(e);
            scene.setQueue(RabbitConfig.SOURCE_QUEUE);
            scene.setConsumerClassName(this.getClass().getName());
            scene.setExchange(RabbitConfig.SOURCE_EXCHANGE);
            scene.setConsumerMethodName(Thread.currentThread().getStackTrace()[1].getMethodName());
            AlarmSceneMessageBody alarmSceneMessageBody = new AlarmSceneMessageBody(scene);
            alarmSceneMessageBody.setWorkspaceCode(dataSourceMessageBody.getWorkspaceCode());
            this.applicationEventPublisher.publishEvent(new AlarmSceneEvent(alarmSceneMessageBody));
            throw e;
        } finally {
            MDC.clear();
        }
    }

}
