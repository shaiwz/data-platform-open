package cn.dataplatform.open.support.store.entity;

import cn.dataplatform.open.common.component.UniversalJsonTypeHandler;
import cn.dataplatform.open.common.vo.alarm.robot.Receive;
import cn.dataplatform.open.common.vo.alarm.robot.Silent;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
    private String category;
    /**
     * BROADCAST广播
     * POLLING轮询
     */
    private String dispatchStrategy;
    /**
     * 接收人
     */
    @TableField(typeHandler = UniversalJsonTypeHandler.class)
    private List<Receive> receives;
    /**
     * 告警沉默关键词
     */
    @TableField(typeHandler = UniversalJsonTypeHandler.class)
    private List<Silent> silent;
    private String status;
    private String recordLogSwitch;
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
