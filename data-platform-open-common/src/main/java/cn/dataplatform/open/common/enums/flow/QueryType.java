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

    CURSOR("1", "游标查询"),
    PAGE("2", "分页查询"),
    SCROLL("3", "滚动查询");

    private final String code;
    private final String name;

    /**
     * 根据编码获取查询类型
     *
     * @param code 查询类型编码
     * @return 查询类型枚举对象，如果未找到则返回 null
     */
    public static QueryType getByCode(String code) {
        for (QueryType value : QueryType.values()) {
            if (Objects.equals(value.getCode(), code)) {
                return value;
            }
        }
        return null;
    }

}