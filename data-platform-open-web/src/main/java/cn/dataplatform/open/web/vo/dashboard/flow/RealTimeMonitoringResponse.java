package cn.dataplatform.open.web.vo.dashboard.flow;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/2
 * @since 1.0.0
 */
@Data
public class RealTimeMonitoringResponse {

    private List<String> keys = Collections.emptyList();


    /**
     * 总量
     */
    private List<BigDecimal> totals = Collections.emptyList();

    /**
     * 速率
     */
    private List<BigDecimal> rates = Collections.emptyList();
    /**
     * 耗时
     */
    private List<Object> costs = Collections.emptyList();
    /**
     * 异常
     */
    private List<Object> errors = Collections.emptyList();

}
