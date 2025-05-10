package cn.dataplatform.open.web.vo.data.source.console;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/27
 * @since 1.0.0
 */
@Data
public class QueryKafkaMqMessageRequest {

    @NotNull
    private Long id;
    @NotBlank
    private String topic;
    private Integer partition;

    private Long offset;

    private Integer maxMessages;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
