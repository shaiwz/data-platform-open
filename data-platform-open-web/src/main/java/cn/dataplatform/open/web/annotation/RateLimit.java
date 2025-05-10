package cn.dataplatform.open.web.annotation;


import cn.dataplatform.open.web.enums.RateLimitStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈接口限流〉
 *
 * @author 丁乾文
 * @date 2021/6/17
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 每个周期内请求次数,默认60秒内一个这个ip地址只能请求一次此接口
     *
     * @return int
     */
    long limit() default 1L;

    /**
     * 周期时间内触发
     *
     * @return int
     */
    long refreshInterval() default 60L;

    /**
     * 限流类型,默认根据ip限制
     *
     * @return RateLimitEnum
     */
    RateLimitStrategy type() default RateLimitStrategy.IP;

    /**
     * 时间单位
     *
     * @return ChronoUnit
     */
    ChronoUnit chronoUnit() default ChronoUnit.SECONDS;

}
