package cn.dataplatform.open.common.annotation;


import cn.dataplatform.open.common.enums.MaskType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 〈Mask〉
 *
 * @author 丁乾文
 * @date 2023/8/14 22:20
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Mask {

    MaskType type() default MaskType.GENERAL;

}
