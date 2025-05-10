package cn.dataplatform.open.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum Status {

    /**
     * 启用,禁用
     */
    ENABLE("启用"),
    DISABLE("禁用");

    private final String name;

}
