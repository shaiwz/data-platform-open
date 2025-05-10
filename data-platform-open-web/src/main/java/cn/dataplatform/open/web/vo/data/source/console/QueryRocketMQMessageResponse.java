package cn.dataplatform.open.web.vo.data.source.console;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/26
 * @since 1.0.0
 */
@Data
public class QueryRocketMQMessageResponse {

    private List<Message> messages = new ArrayList<>();

    /**
     * 耗时ms
     */
    private long cost;

    public void addMessage(Message message) {
        messages.add(message);
    }


}
