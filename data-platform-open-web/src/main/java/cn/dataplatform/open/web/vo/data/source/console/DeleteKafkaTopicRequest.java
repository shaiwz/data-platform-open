package cn.dataplatform.open.web.vo.data.source.console;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/27
 * @since 1.0.0
 */
@Data
public class DeleteKafkaTopicRequest {

    @NotNull
    private Long id;

    private String topic;

}
