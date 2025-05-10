package cn.dataplatform.open.web.store.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息主表实体
 */
@TableName("message")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Message implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 发送者ID
     */
    private Long senderId;

    /**
     * 是否紧急：0否，1是
     */
    @TableField("is_urgent")
    private Integer isUrgent;

    /**
     * 消息状态：PUBLISHED已发布, RECALLED已撤回
     */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}