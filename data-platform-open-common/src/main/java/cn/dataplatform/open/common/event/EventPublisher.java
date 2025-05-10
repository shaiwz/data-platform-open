package cn.dataplatform.open.common.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/15
 * @since 1.0.0
 */
@Component
public class EventPublisher {

    private static ApplicationEventPublisher applicationEventPublisher;

    /**
     * 修复SpringUtil.getBean(ApplicationEventPublisher.class);失败问题
     *
     * @param applicationEventPublisher 事件发布器
     */
    @Autowired
    public void setEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        EventPublisher.applicationEventPublisher = applicationEventPublisher;
    }

    public static void publishEvent(ApplicationEvent event) {
        EventPublisher.applicationEventPublisher.publishEvent(event);
    }

}
