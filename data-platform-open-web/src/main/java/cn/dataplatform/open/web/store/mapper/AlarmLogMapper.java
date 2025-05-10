package cn.dataplatform.open.web.store.mapper;


import cn.dataplatform.open.web.store.entity.AlarmLog;
import cn.dataplatform.open.web.vo.dashboard.base.AlarmServicesProportion;
import cn.dataplatform.open.web.vo.dashboard.base.AlarmStatistics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/18
 * @since 1.0.0
 */
public interface AlarmLogMapper extends BaseMapper<AlarmLog> {


    /**
     * 报警统计 - 按小时
     *
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param workspaceCode 工作空间编码
     * @return r
     */
    List<AlarmStatistics> alarmStatisticsByHour(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                @Param("workspaceCode") String workspaceCode);

    /**
     * 报警统计 - 按分钟
     *
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param workspaceCode 工作空间编码
     * @return r
     */
    List<AlarmStatistics> alarmStatisticsByMinus(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("workspaceCode") String workspaceCode);

    /**
     * 报警统计 - 按天
     *
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param workspaceCode 工作空间编码
     * @return r
     */
    List<AlarmStatistics> alarmStatisticsByDay(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("workspaceCode") String workspaceCode);

    /**
     * 报警服务比例
     *
     * @param localDateTime 时间
     * @param workspaceCode 工作空间
     * @return r
     */
    List<AlarmServicesProportion> alarmServicesProportion(@Param("time") LocalDateTime localDateTime,
                                                          @Param("workspaceCode") String workspaceCode);

}
