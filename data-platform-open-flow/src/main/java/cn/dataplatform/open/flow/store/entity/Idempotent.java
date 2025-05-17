package cn.dataplatform.open.flow.store.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @date 2025/3/15
 * @since 1.0.0
 */
@TableName("idempotent")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Idempotent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 原始消息id
     */
    private String messageId;

    private String workspaceCode;

    private String flowCode;

    private String componentCode;

    private String type;

    private String instanceId;

    private String requestId;

    private LocalDateTime createTime;

    private LocalDateTime expireTime;

}
