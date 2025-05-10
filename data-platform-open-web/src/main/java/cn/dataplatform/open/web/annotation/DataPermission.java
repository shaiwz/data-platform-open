package cn.dataplatform.open.web.annotation;

import cn.dataplatform.open.web.enums.OperationPermissionType;
import cn.dataplatform.open.web.enums.RecordType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/18
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

    /**
     * 提取数据id表达式
     */
    String id() default "";


    /**
     * 记录类型
     */
    RecordType recordType();

    /**
     * 权限类型
     */
    OperationPermissionType type();

}
