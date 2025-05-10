package cn.dataplatform.open.web.annotation;


import cn.dataplatform.open.web.enums.OperationLogAction;
import cn.dataplatform.open.web.enums.OperationLogFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/1
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /**
     * 功能模块
     */
    OperationLogFunction function();

    /**
     * 动作
     */
    OperationLogAction action();

    /**
     * 是否记录请求参数
     */
    boolean requestArg() default true;

    /**
     * 是否记录响应参数
     */
    boolean responseArg() default true;


    /**
     * 提取数据id表达式
     */
    String id() default "";


    /**
     * 是从请求参数中获取，还是从响应参数中获取
     */
    boolean requestExtractId() default true;

}
