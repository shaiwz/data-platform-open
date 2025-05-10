package cn.dataplatform.open.web.vo.dashboard.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/1
 * @since 1.0.0
 */
@Data
public class TopPanelResponse {

    @Data
    public static class DataAlign {
        /**
         * 数量
         */
        private Long taskCount;

        /**
         * 今日执行次数
         */
        private Long todayExecuteCount;

        /**
         * 今日成功率,百分比保留2位小数
         */
        private BigDecimal todaySuccessRate;

        /**
         * 近一周成功率,百分比保留2位小数
         */
        private List<BigDecimal> weekSuccessRate;
    }

    @Data
    public static class DataSource {
        /**
         * 数量
         */
        private Long count;
        /**
         * 最近一次监控检查时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastHealthCheckTime;

        /**
         * 今日成功率,百分比保留2位小数
         */
        private BigDecimal todaySuccessRate;

        /**
         * 近一周成功率,百分比保留2位小数
         */
        private List<BigDecimal> weekSuccessRate;
    }

    @Data
    public static class DataFlow {
        /**
         * 运行中
         */
        private Long runningCount;
        /**
         * 待发布 无版本号
         */
        private Long waitingCount;
        /**
         * 已暂停
         */
        private Long pauseCount;
    }

    @Data
    public static class QueryTemplate {
        /**
         * 已发布
         */
        private Long count;
        /**
         * 待发布 无版本号
         */
        private Long waitingCount;
        /**
         * 已停用
         */
        private Long pauseCount;
    }

}
