package cn.dataplatform.open.web.store.mapper;

import cn.dataplatform.open.web.store.entity.OperationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    /**
     * 活跃成员
     *
     * @param workspaceCode 工作空间编码
     * @param startTime     s
     * @param endTime       e
     * @return r
     */
    @Select("""
            SELECT user_id as userId 
                    FROM operation_log
                    WHERE create_time BETWEEN #{startTime} AND #{endTime}
                    and workspace_code = #{workspaceCode}
                    GROUP BY user_id
                    ORDER BY COUNT(*) DESC
                    LIMIT 6
            """)
    List<Long> activeMembers(@Param("workspaceCode") String workspaceCode,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);

}
