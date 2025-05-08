package cn.dataplatform.open.common.util;

import org.springframework.scheduling.support.CronExpression;

import java.time.ZonedDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/25
 * @since 1.0.0
 */
public class CronUtils {

    /**
     * 校验cron表达式是否有效
     *
     * @param cronExpression cron表达式
     * @return 是否有效
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * 校验cron表达式，无效时抛出异常
     *
     * @param cronExpression cron表达式
     * @throws IllegalArgumentException 如果表达式无效
     */
    public static void validate(String cronExpression) {
        if (!isValid(cronExpression)) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronExpression);
        }
    }

    /**
     * 获取下一个执行时间
     *
     * @param cronExpression cron表达式
     * @param after          从哪个时间点之后开始计算
     * @return 下一个执行时间
     */
    public static ZonedDateTime nextExecutionTime(String cronExpression, ZonedDateTime after) {
        CronExpression expression = CronExpression.parse(cronExpression);
        return expression.next(after);
    }

    /**
     * 获取下一个执行时间（从当前时间开始计算）
     *
     * @param cronExpression cron表达式
     * @return 下一个执行时间
     */
    public static ZonedDateTime nextExecutionTime(String cronExpression) {
        return nextExecutionTime(cronExpression, ZonedDateTime.now());
    }

}
