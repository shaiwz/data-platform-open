package cn.dataplatform.open.web.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class MyMessageResponse {

    private Long id;

    private Long muId;
    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：SYSTEM系统消息, NOTICE通知, REMIND提醒
     */
    private String messageType;

    /**
     * 发送范围：ALL全员, WORKSPACE工作空间, SPECIFIC特定用户
     */
    private String scopeType;

    /**
     * 是否紧急：0否，1是
     */
    private Integer isUrgent;

    /**
     * 消息状态：PUBLISHED已发布, RECALLED已撤回
     */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Integer read;

    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

}
