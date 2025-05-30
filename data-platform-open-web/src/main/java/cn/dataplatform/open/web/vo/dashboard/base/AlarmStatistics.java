package cn.dataplatform.open.web.vo.dashboard.base;

import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/1
 * @since 1.0.0
 */
@Data
public class AlarmStatistics {

    private String key;

    private Long successCount;
    private Long failedCount;
    private Long silentCount;

}
