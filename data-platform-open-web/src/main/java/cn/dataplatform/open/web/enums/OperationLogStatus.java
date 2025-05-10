package cn.dataplatform.open.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/5/4
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum OperationLogStatus {

    /**
     * 成功
     */
    SUCCESS("成功"),
    /**
     * 失败
     */
    FAIL("失败"),
    ;

    private final String name;

}
