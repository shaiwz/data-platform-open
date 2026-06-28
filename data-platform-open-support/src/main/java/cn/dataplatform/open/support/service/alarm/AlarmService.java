package cn.dataplatform.open.support.service.alarm;

import cn.dataplatform.open.common.body.AlarmMessageBody;
import cn.dataplatform.open.support.store.entity.AlarmScene;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/22
 * @since 1.0.0
 */
public interface AlarmService {

    /**
     * 告警
     *
     * @param alarmMessageBody 告警消息
     */
    void alarm(AlarmMessageBody alarmMessageBody);

    /**
     * 异步执行告警
     *
     * @param alarmMessageBody 告警消息
     * @param alarmScene       告警场景
     */
    void alarmAsync(AlarmMessageBody alarmMessageBody, AlarmScene alarmScene);

    /**
     * 告警
     *
     * @param alarmMessageBody 告警消息
     * @param alarmScene       告警场景
     */
    void alarm(AlarmMessageBody alarmMessageBody, AlarmScene alarmScene);

}
