package cn.dataplatform.open.common.enums.alarm;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/22
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum AlarmRobotDispatchStrategy {

    /**
     * BROADCAST广播
     * POLLING轮询
     */
    BROADCAST("广播"),
    POLLING("轮询"),
    /**
     * 随机
     */
    RANDOM("随机");

    private final String name;

}
