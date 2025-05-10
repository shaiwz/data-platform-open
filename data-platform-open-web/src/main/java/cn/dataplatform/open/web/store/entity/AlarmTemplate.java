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
 * @date 2025/2/19
 * @since 1.0.0
 */
@TableName("alarm_template")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AlarmTemplate implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private String type;

    private String mode;

    private String status;

    /**
     * 外部系统的模板编码,例如飞书的消息卡片
     */
    private String externalTemplateCode;

    private String templateContent;

    private String description;

    private String workspaceCode;

    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
