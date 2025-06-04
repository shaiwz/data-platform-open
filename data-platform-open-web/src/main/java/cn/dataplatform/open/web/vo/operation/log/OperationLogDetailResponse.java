package cn.dataplatform.open.web.vo.operation.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/9
 * @since 1.0.0
 */
@Data
public class OperationLogDetailResponse {

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

    /**
     * 异常
     */
    private String exception;

    private String status;

    /**
     * 耗时,单位毫秒
     */
    private Long cost;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
