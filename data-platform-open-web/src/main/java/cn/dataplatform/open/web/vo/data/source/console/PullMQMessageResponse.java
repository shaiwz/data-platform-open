package cn.dataplatform.open.web.vo.data.source.console;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/25
 * @since 1.0.0
 */
@Data
public class PullMQMessageResponse {

    private List<Message> messages = new ArrayList<>();
    private Long messageCount = 0L;

    /**
     * 耗时ms
     */
    private long cost;

    public void addMessage(Message message) {
        messages.add(message);
    }


}
