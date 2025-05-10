package cn.dataplatform.open.web.vo.message;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/29
 * @since 1.0.0
 */
@Data
public class MessageListRequest {

    private String title;

    private String messageType;

    private Integer isUrgent;

    /**
     * 消息状态：PUBLISHED已发布, RECALLED已撤回
     */
    private String status;

    private LocalDateTime startCreateTime;

    private LocalDateTime endCreateTime;

    /**
     * 读取状态
     */
    private Integer read;

}
