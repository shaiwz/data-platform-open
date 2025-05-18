package cn.dataplatform.open.common.alarm.scene;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 全局未知异常处理
 *
 * @author dingqianwen
 * @date 2025/2/22
 * @since 1.0.0
 */
@NoArgsConstructor
@Data
public class GlobalExceptionHandlingScene implements Scene {

    @Serial
    private static final long serialVersionUID = 1L;

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

    public GlobalExceptionHandlingScene(Throwable throwable) {
        if (throwable == null) {
            return;
        }
        this.exceptionName = throwable.getClass().getSimpleName();
        this.exceptionMessage = throwable.getMessage();
        this.exceptionStackTrace = ExceptionUtil.stacktraceToString(throwable, 2000);
    }


    @Override
    public String scene() {
        return "GLOBAL_EXCEPTION_HANDLING";
    }

}
