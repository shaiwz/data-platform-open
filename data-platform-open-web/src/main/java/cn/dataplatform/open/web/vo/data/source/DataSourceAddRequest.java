package cn.dataplatform.open.web.vo.data.source;


import cn.dataplatform.open.common.enums.DataSourceType;
import cn.dataplatform.open.common.enums.Status;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@Data
public class DataSourceAddRequest {

    @NotEmpty
    @Size(min = 1, max = 32)
    private String name;

    /**
     * MySQL Doris StarRocks Oracle等
     *
     * @see DataSourceType
     */
    @NotEmpty
    private String type;

    @Size(max = 255)
    private String url;

    @Size(max = 128)
    private String driver;

    @Size(max = 32)
    private String username;

    @Size(max = 128)
    private String password;

    private Integer maxPoolSize;

    /**
     * 启用,禁用
     *
     * @see Status
     */
    @NotEmpty
    @Pattern(regexp = "^(ENABLE|DISABLE)$")
    private String status;

    private String feNodes;
    private String beNodes;
    /**
     * 分表规则
     */
    private String partitioningAlgorithm;

    private String healthCheck;

    private List<MarkColumn> maskColumn;

    private String description;

}
