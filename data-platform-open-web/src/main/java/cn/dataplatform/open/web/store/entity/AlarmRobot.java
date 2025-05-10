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
@TableName("alarm_robot")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AlarmRobot implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * uuid
     */
    private String code;
    private String name;
    private String type;
    /**
     * BROADCAST广播
     * POLLING轮询
     */
    private String mode;
    /**
     * json
     */
    private String receives;
    /**
     * json
     */
    private String silent;
    private String status;
    private String recordLogSwitch;
    /**
     * 告警去重
     * ENABLE启用,开启后,如果重复数据,日志也不记录
     */
    //private String alarmDeDuplication;
    /**
     * 去重复检测间隔,单位秒
     */
    //private Long deDuplicationInterval;

    private String workspaceCode;
    private String description;
    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
