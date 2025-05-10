package cn.dataplatform.open.common.alarm.scene;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 启动流程失败告警
 *
 * @author dingqianwen
 * @date 2025/4/3
 * @since 1.0.0
 */
@NoArgsConstructor
@Data
public class StartFlowFailNoticeScene implements Scene {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 流程名称
     */
    private String flowName;
    /**
     * 流程编码
     */
    private String flowCode;
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

    public StartFlowFailNoticeScene(Throwable throwable) {
        if (throwable != null) {
            this.exceptionName = throwable.getClass().getSimpleName();
            this.exceptionMessage = throwable.getMessage();
            this.exceptionStackTrace = ExceptionUtil.stacktraceToString(throwable, 2000);
        }
    }

    @Override
    public String scene() {
        return "START_FLOW_FAIL_NOTICE";
    }

}
