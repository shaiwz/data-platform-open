package cn.dataplatform.open.common.event;

import cn.dataplatform.open.common.body.QueryTemplateMessageBody;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

public class QueryTemplateEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1628296277627810450L;

    public QueryTemplateEvent(QueryTemplateMessageBody queryTemplateMessageBody) {
        super(queryTemplateMessageBody);
    }

    @Override
    public QueryTemplateMessageBody getSource() {
        return (QueryTemplateMessageBody) super.getSource();
    }
}
