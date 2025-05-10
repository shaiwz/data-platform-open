package cn.dataplatform.open.web.vo.dashboard;

import lombok.Data;

import java.math.BigDecimal;
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
public class ServerInfoResponse {

    private InstanceInfo instanceInfo;
    private Cpu cpu;
    private Memory memory;

    @Data
    public static class InstanceInfo {
        /**
         * 运行中
         */
        private Long runningCount;

        /**
         * 离线
         */
        private Long offlineCount;
    }

    @Data
    public static class Cpu {
        /**
         * 核心数
         */
        private Integer core;
        /**
         * 占用率
         */
        private BigDecimal usageRate;
        private List<String> last10MinutesLabels;
        /**
         * 最近10分钟占用率
         */
        private List<BigDecimal> last10MinutesUsageRate;
    }

    @Data
    public static class Memory {
        /**
         * 总内存
         */
        private BigDecimal total;
        /**
         * 剩余
         */
        private BigDecimal free;
        /**
         * 使用率
         */
        private BigDecimal usageRate = BigDecimal.ZERO;

        private List<String> last10MinutesLabels;

        /**
         * 最近10分钟G
         */
        private List<BigDecimal> last10MinutesUsage;
    }

}
