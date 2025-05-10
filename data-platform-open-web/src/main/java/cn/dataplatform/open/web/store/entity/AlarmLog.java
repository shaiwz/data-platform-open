package cn.dataplatform.open.web.store.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/18
 * @since 1.0.0
 */
@TableName("alarm_log")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AlarmLog implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String requestId;
    private String robotCode;
    private String templateCode;
    private String sceneCode;
    private String serverName;
    private String instanceId;

    /**
     * 发送中,发送完毕,发送失败
     *
     * @see cn.dataplatform.open.common.enums.AlarmLogStatus
     */
    private String status;
    /**
     * 失败原因
     * max length is 500
     */
    private String errorReason;

    private String workspaceCode;
    private String parameter;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
