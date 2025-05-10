package cn.dataplatform.open.web.vo.dashboard.base;

import lombok.Data;

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
public class AlarmStatisticsResponse {

    /**
     * 单位
     */
    private String unit;

    private List<AlarmStatistics> alarmStatistics;

}
