package cn.dataplatform.open.common.enums;

import lombok.AllArgsConstructor;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/22
 * @since 1.0.0
 */
@AllArgsConstructor
public enum AlarmLogStatus {
    /**
     * 发送中,发送完毕,发送失败
     */
    SENDING("发送中"),
    SUCCESS("发送完毕"),
    /**
     * 消息沉默,不发送
     */
    SILENT("沉默"),
    FAILED("发送失败");

    private final String name;

}
