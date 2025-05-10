package cn.dataplatform.open.common.enums.flow;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/30
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum QueryType {

    STREAMING("1", "流式查询"),
    PAGE("2", "分页查询"),
    SCROLL("3", "滚动查询");

    private final String code;
    private final String name;

    public static QueryType getByCode(String code) {
        for (QueryType value : QueryType.values()) {
            if (Objects.equals(value.getCode(), code)) {
                return value;
            }
        }
        return null;
    }

}
