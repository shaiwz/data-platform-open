package cn.dataplatform.open.web.exception;

import cn.dataplatform.open.common.enums.ErrorCode;
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
public class ReSubmitException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6405345374923437770L;

    private final int code;

    public ReSubmitException() {
        super(ErrorCode.DP_10011038.getMsg());
        this.code = ErrorCode.DP_10011038.getCode();
    }

}
