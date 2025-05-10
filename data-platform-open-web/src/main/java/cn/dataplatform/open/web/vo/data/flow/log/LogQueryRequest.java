package cn.dataplatform.open.web.vo.data.flow.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/8
 * @since 1.0.0
 */
@Data
public class LogQueryRequest {

    @NotBlank
    private String flowCode;

    private String requestId;

    @Max(5000)
    @NotNull
    private Long limit = 5000L;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;

    private String level;

    private List<String> keywords;

}
