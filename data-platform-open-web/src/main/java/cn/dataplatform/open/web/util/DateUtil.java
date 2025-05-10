package cn.dataplatform.open.web.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/4
 * @since 1.0.0
 */
public class DateUtil extends cn.hutool.core.date.DateUtil {

    /**
     * 时差,格式返回例如1d1h1s
     *
     * @param startTime 开始时间
     * @param now       当前时间
     * @return 时差
     */
    public static String dashboardTimeDifference(LocalDateTime startTime, LocalDateTime now) {
        long seconds = startTime.until(now, java.time.temporal.ChronoUnit.SECONDS);
        long days = seconds / (60 * 60 * 24);
        long hours = (seconds % (60 * 60 * 24)) / (60 * 60);
        long minute = (seconds % (60 * 60)) / 60;
        long second = seconds % 60;
        StringBuilder stringBuilder = new StringBuilder("[");
        if (days > 0) {
            stringBuilder.append(days).append("d");
        }
        if (hours > 0) {
            stringBuilder.append(hours).append("h");
        }
        if (minute > 0) {
            stringBuilder.append(minute).append("m");
        }
        if (second > 0) {
            stringBuilder.append(second).append("s");
        }
        return stringBuilder.append("]").toString();
    }

    /**
     * 获取时间间隔,单位秒
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时间间隔
     */
    public static int getDashboardStep(LocalDateTime startTime, LocalDateTime endTime) {
        // 如果小于10分钟,则返回10
        long seconds = startTime.until(endTime, java.time.temporal.ChronoUnit.SECONDS);
        // 小于1小时返回1分钟
        if (seconds < 3600) {
            return 60;
        }
        // 小于2小时,返回5分钟
        if (seconds < 7200) {
            return 300;
        }
        // 小于10小时,返回30分钟
        if (seconds < 36000) {
            return 1800;
        }
        // 小于2天返回1小时
        if (seconds < 172800) {
            return 3600;
        }
        // 小于1周,返回4小时
        if (seconds < (86400 * 12)) {
            return 3600 * 4;
        }
        // 剩余返回1天
        return 86400;
    }

    /**
     * 时间格式化
     *
     * @param step       步长
     * @param timeObject 时间
     * @return 时间格式化
     */
    public static String timeFormat(int step, Object timeObject) {
        if (step == 86400) {
            return timeFormat(timeObject, "yy-MM-dd");
        } else if (step == 3600 * 4) {
            return timeFormat(timeObject, "dd HH");
        } else if (step == 3600) {
            return timeFormat(timeObject, "dd HH:mm");
        } else if (step == 1800) {
            return timeFormat(timeObject, "dd HH:mm");
        } else if (step == 300) {
            return timeFormat(timeObject, "dd HH:mm");
        } else if (step == 60) {
            return timeFormat(timeObject, "HH:mm");
        } else {
            return timeFormat(timeObject, "HH:mm:ss");
        }
    }

    /**
     * 时间格式化
     *
     * @param timeObject 时间
     * @param timeFormat 格式
     * @return 时间格式化
     */
    public static String timeFormat(Object timeObject, String timeFormat) {
        DateTime dateTime;
        if (timeObject instanceof LocalDateTime localDateTime) {
            dateTime = new DateTime(localDateTime);
        } else {
            String timeString = StrUtil.padAfter(String.valueOf(timeObject), 13, '0');
            dateTime = new DateTime(timeString);
        }
        return dateTime.toString(timeFormat);
    }


    /**
     * 将单个 LocalDateTime 转换为纳秒级时间戳
     *
     * @param localDateTime 要转换的 LocalDateTime 对象
     * @return 对应的纳秒级时间戳
     */
    public static long convertToNanoTimestamp(LocalDateTime localDateTime) {
        // 将 LocalDateTime 转换为 ZonedDateTime,使用系统默认时区
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        // 获取 Instant 对象
        java.time.Instant instant = zonedDateTime.toInstant();
        // 计算纳秒级时间戳
        return instant.getEpochSecond() * 1_000_000_000L + instant.getNano();
    }

}
