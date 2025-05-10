package cn.dataplatform.open.common.event;

import cn.dataplatform.open.common.body.DataAlignMessageBody;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

public class DataAlignEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1628296277627810450L;

    public DataAlignEvent(DataAlignMessageBody dataAlignMessageBody) {
        super(dataAlignMessageBody);
    }

    @Override
    public DataAlignMessageBody getSource() {
        return (DataAlignMessageBody) super.getSource();
    }
}
