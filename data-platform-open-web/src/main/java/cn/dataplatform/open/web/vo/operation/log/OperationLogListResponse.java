package cn.dataplatform.open.web.vo.operation.log;

import cn.dataplatform.open.web.vo.user.UserData;
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
public class OperationLogListResponse {

    private Long id;
    private UserData user;

    /**
     * 工作空间编码
     */
    private String workspaceCode;
    private String workspaceName;

    private String function;

    private String action;

    private String requestId;

    /**
     * 耗时,单位毫秒
     */
    private Long cost;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
