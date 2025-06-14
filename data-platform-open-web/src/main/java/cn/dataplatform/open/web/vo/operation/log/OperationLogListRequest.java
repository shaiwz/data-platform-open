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
public class OperationLogListRequest {

    private String requestId;

    private Long userId;
    private String username;

    /**
     * startTime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startCreateTime;

    /**
     * endTime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endCreateTime;

    private String function;

    private String action;

    /**
     * 成功、失败
     */
    private String status;

}
