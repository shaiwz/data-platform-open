package cn.dataplatform.open.web.vo.dashboard.base;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/1
 * @since 1.0.0
 */
@Data
public class SuccessRate {
    private LocalDate date;
    private BigDecimal successRate;
}
