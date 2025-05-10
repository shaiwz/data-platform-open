package cn.dataplatform.open.web.store.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * @date 2025/2/16
 * @since 1.0.0
 */
@TableName("debezium_save_point")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DebeziumSavePoint implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String workspaceCode;

    private String flowCode;

    private String componentCode;

    private String instanceId;

    /**
     * 保存点唯一编码
     */
    private String savePoint;

    @TableField("`key`")
    private String key;

    private String value;

    private LocalDateTime createTime;

    /**
     * 到期时间
     */
    private LocalDateTime expireTime;

}
