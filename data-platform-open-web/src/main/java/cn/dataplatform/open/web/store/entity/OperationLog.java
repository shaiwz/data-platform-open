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
 * @date 2025/3/1
 * @since 1.0.0
 */
@TableName("operation_log")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OperationLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 操作人
     */
    private String username;
    private Long userId;

    /**
     * 工作空间编码
     */
    private String workspaceCode;
    private String workspaceName;

    @TableField("`function`")
    private String function;

    private String action;

    /**
     * 操作的数据id
     */
    private Long recordId;

    private String requestArg;

    private String responseArg;

    private String requestId;

    private String className;

    private String methodName;

    private String status;
    /**
     * 异常
     */
    private String exception;

    /**
     * 耗时,单位毫秒
     */
    private Long cost;

    private LocalDateTime createTime;

}
