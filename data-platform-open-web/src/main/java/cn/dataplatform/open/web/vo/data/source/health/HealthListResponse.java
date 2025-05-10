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
public class HealthListResponse {

    private Long id;

    private String requestId;

    private String dataSourceCode;

    private String dataSourceName;

    private String errorReason;

    /**
     * 健康状态,异常,正常,超时
     *
     * @see cn.dataplatform.open.common.enums.DataSourceHealthLogStatus
     */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
