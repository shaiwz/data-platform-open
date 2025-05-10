package cn.dataplatform.open.common.event;

import cn.dataplatform.open.common.body.AlarmSceneMessageBody;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/22
 * @since 1.0.0
 */
public class AlarmSceneEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1628296277627810450L;

    public AlarmSceneEvent(AlarmSceneMessageBody source) {
        super(source);
    }

    @Override
    public AlarmSceneMessageBody getSource() {
        return (AlarmSceneMessageBody) super.getSource();
    }

}
