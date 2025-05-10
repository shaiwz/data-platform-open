package cn.dataplatform.open.web.vo.data.source.health;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/28
 * @since 1.0.0
 */
@Data
public class HealthListRequest {

    private String dataSourceCode;

    /**
     * 健康状态,异常,正常,超时
     *
     * @see cn.dataplatform.open.common.enums.DataSourceHealthLogStatus
     */
    private String status;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startCreateTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endCreateTime;

}
