package cn.dataplatform.open.web.vo.data.source;


import cn.dataplatform.open.common.enums.DataSourceType;
import cn.dataplatform.open.common.enums.Status;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@Data
public class DataSourceListResponse {

    private Long id;

    private String name;

    private String code;

    /**
     * MySQL Doris StarRocks Oracle等
     *
     * @see DataSourceType
     */
    private String type;

    private String url;

    private String username;

    private String driver;

    /**
     * 启用,禁用
     *
     * @see Status
     */
    private String status;

    private String feNodes;
    private String beNodes;

    private String healthCheck;

    private String maskColumn;

    private Long createUserId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
