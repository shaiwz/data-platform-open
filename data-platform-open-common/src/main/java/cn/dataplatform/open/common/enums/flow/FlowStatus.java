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

    TBP("待发布"),
    /**
     * 启用, 启用中的数据流
     */
    ENABLE("启用"),
    /**
     * 已被手动暂停的数据流
     */
    PAUSE("暂停"),
    /**
     * 历史版本的数据流
     */
    HISTORY("历史"),
    ;

    private final String name;

}
