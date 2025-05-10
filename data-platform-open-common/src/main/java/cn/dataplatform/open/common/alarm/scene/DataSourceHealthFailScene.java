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
 * @date 2025/3/8
 * @since 1.0.0
 */
@NoArgsConstructor
@Data
public class DataSourceHealthFailScene implements Scene {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据源名称
     */
    private String name;
    /**
     * 数据源编码
     */
    private String code;
    /**
     * 数据源类型
     */
    private String type;
    /**
     * 数据源状态
     */
    private String status;
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


    public DataSourceHealthFailScene(Throwable throwable) {
        if (throwable == null) {
            return;
        }
        this.exceptionName = throwable.getClass().getSimpleName();
        this.exceptionMessage = throwable.getMessage();
        this.exceptionStackTrace = ExceptionUtil.stacktraceToString(throwable, 2000);
    }

    @Override
    public String scene() {
        return "DATA_SOURCE_HEALTH_FAIL";
    }

}
