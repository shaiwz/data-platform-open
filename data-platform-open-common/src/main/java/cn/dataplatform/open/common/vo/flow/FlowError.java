package cn.dataplatform.open.common.vo.flow;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/3
 * @since 1.0.0
 */
@Data
public class FlowError implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private ErrorType type;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    private String message;

    private String instanceId;

    /**
     * 错误类型
     */
    @AllArgsConstructor
    @Getter
    public enum ErrorType implements Serializable {

        /**
         * 启动失败
         */
        STARTUP("启动失败"),

        /**
         * 运行异常，但是仍然可以继续
         */
        RUNNING("运行异常"),
        /**
         * 警告，仍然可以运行，例如只是缺少节点等
         */
        WARNING("警告"),

        /**
         * 异常中断，数据流运行崩溃，从引擎中移除，等待服务器重启，或者下次发布
         */
        ABORT("异常中断"),
        ;
        @Serial
        private static final long serialVersionUID = 1L;

        private final String name;
    }

}
