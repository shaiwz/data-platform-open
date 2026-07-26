package cn.dataplatform.open.common.util;

import org.springframework.scheduling.support.CronExpression;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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
     * 获取下x次执行时间
     *
     * @param cronExpression cron表达式
     * @param after          从哪个时间点之后开始计算
     * @return 下x次执行时间
     */
    public static List<ZonedDateTime> nextExecutionTime(String cronExpression, ZonedDateTime after,
                                                        int times) {
        CronExpression expression = CronExpression.parse(cronExpression);
        List<ZonedDateTime> list = new ArrayList<>();
        ZonedDateTime current = after;
        for (int i = 0; i < times; i++) {
            current = expression.next(current);
            // 如果算不出下一次时间，直接中断
            if (current == null) {
                break;
            }
            list.add(current);
        }
        return list;
    }

}
