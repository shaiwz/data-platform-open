package cn.dataplatform.open.web.vo.data.source.console;

import lombok.Data;

import java.util.Date;
import java.util.List;
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
public class QueryKafkaMqMessageResponse {

    private List<Message> messages;
    private Integer messageCount;
    private Long cost;

    @Data
    public static class Message {
        private String topic;
        private Integer partition;
        private Long offset;
        private String key;
        private String value;
        private Date timestamp;
        private Map<String, String> headers;
    }

}
