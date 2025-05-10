package cn.dataplatform.open.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/17
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum Authority {

    /**
     * 有权限/或者没权限
     */
    YES("有权限"),
    NO("没权限");

    private final String name;
}
