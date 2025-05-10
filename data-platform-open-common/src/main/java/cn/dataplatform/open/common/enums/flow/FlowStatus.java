package cn.dataplatform.open.common.enums.flow;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/6
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum FlowStatus {
    /**
     * 启用,禁用,暂停
     */
    ENABLE("启用"),
    DISABLE("禁用"),
    PAUSE("暂停");

    private final String name;

}
