package cn.dataplatform.open.web.vo.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/29
 * @since 1.0.0
 */
@Data
public class MessageSendRequest {

    /**
     * 发送给具体人
     */
    private List<Long> userIds;

    /**
     * 发送给工作空间下的人
     */
    private Long workspaceId;

    /**
     * 消息标题
     */
    @NotBlank
    @Length(max = 50)
    private String title;

    /**
     * 消息内容
     */
    @NotBlank
    @Length(max = 200)
    private String content;

    /**
     * 消息类型：SYSTEM系统消息, NOTICE通知, REMIND提醒
     */
    @NotBlank
    private String messageType;

    /**
     * 是否紧急：0否，1是
     */
    private Integer isUrgent;

}
