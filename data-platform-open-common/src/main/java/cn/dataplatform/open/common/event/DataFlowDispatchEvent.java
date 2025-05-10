package cn.dataplatform.open.common.event;

import cn.dataplatform.open.common.body.DataFlowDispatchMessageBody;
import org.springframework.context.ApplicationEvent;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/25
 * @since 1.0.0
 */
public class DataFlowDispatchEvent extends ApplicationEvent {

    public DataFlowDispatchEvent(DataFlowDispatchMessageBody source) {
        super(source);
    }

    @Override
    public DataFlowDispatchMessageBody getSource() {
        return (DataFlowDispatchMessageBody) super.getSource();
    }

}
