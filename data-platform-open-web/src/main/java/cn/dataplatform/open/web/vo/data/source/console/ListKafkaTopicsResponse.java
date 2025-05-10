package cn.dataplatform.open.web.vo.data.source.console;

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
public class ListKafkaTopicsResponse {

    private String name;
    private Integer partitions;
    private Integer replicas;
    private Map<String, String> configs;
}
