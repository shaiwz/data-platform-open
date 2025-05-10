package cn.dataplatform.open.web.vo.dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/21
 * @since 1.0.0
 */
@Data
public class AllServerMemoryResponse {

    private List<ServerMemory> serverMemories;

    @Data
    public static class ServerMemory {
        private String instanceId;
        private BigDecimal currentUsageRate;
        private List<String> last10MinutesLabels;
        private List<BigDecimal> last10MinutesUsage;
        private BigDecimal total;
        private BigDecimal used;
    }

}
