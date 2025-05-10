package cn.dataplatform.open.web.vo.data.source.console;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/27
 * @since 1.0.0
 */
@Data
public class UpdateKafkaTopicRequest {

    @NotNull
    private Long id;

    private String name;
    private Integer numPartitions;
    private Integer replicationFactor;
    private Map<String, String> configs;

}
