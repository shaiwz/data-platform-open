package cn.dataplatform.open.web.vo.dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/20
 * @since 1.0.0
 */
@Data
public class AllServerCpuRateResponse {

    private List<ServerCpuRate> serverCpuRates;

    @Data
    public static class ServerCpuRate {
        private String instanceId;
        private int coreCount;
        private BigDecimal currentUsageRate;
        private List<String> last10MinutesLabels;
        private List<BigDecimal> last10MinutesUsageRate;
    }

}
