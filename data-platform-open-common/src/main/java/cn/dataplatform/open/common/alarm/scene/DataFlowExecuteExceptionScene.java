package cn.dataplatform.open.common.alarm.scene;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/23
 * @since 1.0.0
 */
@NoArgsConstructor
@Data
public class DataFlowExecuteExceptionScene implements DataFlowScene {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String SCENE = "DATA_FLOW_EXECUTE_EXCEPTION";

    /**
     * 数据流编码
     */
    private String flowCode;
    /**
     * 数据流名称
     */
    private String flowName;

    /**
     * 组件编码
     */
    private String componentCode;
    /**
     * 组件名称
     */
    private String componentName;

    /**
     * 组件类名称
     */
    private String componentClassName;

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

    public DataFlowExecuteExceptionScene(Throwable throwable) {
        if (throwable != null) {
            this.exceptionName = throwable.getClass().getSimpleName();
            this.exceptionMessage = throwable.getMessage();
            this.exceptionStackTrace = ExceptionUtil.stacktraceToString(throwable, 2000);
        }
    }

    /**
     * 场景名称
     *
     * @return 场景
     */
    @Override
    public String scene() {
        return SCENE;
    }

    /**
     * 当前告警场景关联的数据流编码
     *
     * @return 数据流编码
     */
    @Override
    public String getFlowCode() {
        return this.flowCode;
    }

}