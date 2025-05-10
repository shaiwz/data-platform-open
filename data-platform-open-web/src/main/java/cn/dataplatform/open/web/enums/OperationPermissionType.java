package cn.dataplatform.open.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/18
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum OperationPermissionType {

    /**
     * 编辑权限
     */
    EDIT("编辑权限"),

    /**
     * 发布权限
     */
    PUBLISH("发布权限");

    private final String name;

}
