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
 * @date 2025/3/26
 * @since 1.0.0
 */
@Data
public class QueryRocketMQMessageRequest {
    @NotNull
    private Long id;

    @NotBlank
    private String topic;

    @NotBlank
    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private String messageId;

    private String messageKey;

}
