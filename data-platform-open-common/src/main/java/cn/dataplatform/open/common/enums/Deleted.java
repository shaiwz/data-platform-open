package cn.dataplatform.open.common.enums;

import lombok.Getter;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2020/7/14
 * @since 1.0.0
 */
@Getter
public enum Deleted {

    /**
     * ENABLE:未被删除
     */
    ENABLE(0), DISABLE(1);

    private final Integer status;

    Deleted(Integer status) {
        this.status = status;
    }

}
