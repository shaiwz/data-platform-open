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
 * @date 2025/3/8
 * @since 1.0.0
 */
@TableName("debezium_schema_history")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DebeziumSchemaHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String workspaceCode;

    private String flowCode;

    private String componentCode;

    private String instanceId;


    private String schemaLine;



    private LocalDateTime createTime;

    /**
     * 到期时间
     */
    private LocalDateTime expireTime;

}
