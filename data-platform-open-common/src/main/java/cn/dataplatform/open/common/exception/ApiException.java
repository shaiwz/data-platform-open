package cn.dataplatform.open.common.exception;

import cn.dataplatform.open.common.enums.ErrorCode;
import cn.hutool.core.text.StrFormatter;
import lombok.Getter;

import java.io.Serial;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 丁乾文
 * @date 2020/1/10
 * @since 1.0.0
 */
@Getter
public class ApiException extends RuntimeException {


    @Serial
    private static final long serialVersionUID = 6405345374923437770L;

    private final int code;

    public ApiException(ErrorCode statusCode) {
        super(statusCode.getMsg());
        this.code = statusCode.getCode();
    }

    public ApiException(int code, String message, Object... args) {
        super(StrFormatter.format(message, args));
        this.code = code;
    }

    public ApiException(Throwable e) {
        super(e);
        this.code = ErrorCode.DP_500.getCode();
    }

    /**
     * 例如:
     * <blockquote>
     * throw new ApiException("根据Name: {}, 没有查询到数据",name);
     * </blockquote>
     *
     * @param message 异常消息
     * @param args    消息中参数
     */
    public ApiException(String message, Object... args) {
        super(StrFormatter.format(message, args));
        this.code = ErrorCode.DP_99990101.getCode();
    }

    /**
     * 例如:
     * <blockquote>
     * throw new ApiException("没有查询到数据", e);
     * </blockquote>
     *
     * @param message 异常消息
     * @param e       异常
     */
    public ApiException(String message, Throwable e) {
        super(message, e);
        this.code = ErrorCode.DP_500.getCode();
    }

}
