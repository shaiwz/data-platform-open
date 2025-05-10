package cn.dataplatform.open.common.event;

import cn.dataplatform.open.common.body.DataSourceMessageBody;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

public class DataSourceEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1628296277627810450L;

    public DataSourceEvent(DataSourceMessageBody dataSourceMessageBody) {
        super(dataSourceMessageBody);
    }

    @Override
    public DataSourceMessageBody getSource() {
        return (DataSourceMessageBody) super.getSource();
    }

}
