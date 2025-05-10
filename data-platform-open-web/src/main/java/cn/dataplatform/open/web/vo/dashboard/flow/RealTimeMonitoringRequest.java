package cn.dataplatform.open.web.vo.dashboard.flow;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/2
 * @since 1.0.0
 */
@Data
public class RealTimeMonitoringRequest {

    /**
     * 时间范围
     */
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime endTime;

    private String flowCode;

    private String componentCode;

}
