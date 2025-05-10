package cn.dataplatform.open.common.alarm.scene;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 服务通知消息消费异常
 *
 * @author dingqianwen
 * @date 2025/2/23
 * @since 1.0.0
 */
@NoArgsConstructor
@Data
public class ServerNoticeMessageExceptionScene implements Scene {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 队列名称
     */
    private String queue;
    /**
     * 交换机
     */
    private String exchange;

    /**
     * 消费类名称
     */
    private String consumerClassName;
    /**
     * 消费方法名称
     */
    private String consumerMethodName;

    /**
     * 异常名称
     */
    private String exceptionName;

    /**
     * 异常消息
     */
    private String exceptionMessage;

    /**
     * 异常堆栈,最多2000字符
     */
    private String exceptionStackTrace;

    public ServerNoticeMessageExceptionScene(Throwable throwable) {
        if (throwable == null) {
            return;
        }
        this.exceptionName = throwable.getClass().getSimpleName();
        this.exceptionMessage = throwable.getMessage();
        this.exceptionStackTrace = ExceptionUtil.stacktraceToString(throwable, 2000);
    }

    @Override
    public String scene() {
        return "SERVER_NOTICE_MESSAGE_EXCEPTION";
    }

}
