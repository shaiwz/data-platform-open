package cn.dataplatform.open.web.vo.data.source;


import cn.dataplatform.open.common.annotation.Mask;
import cn.dataplatform.open.common.enums.DataSourceType;
import cn.dataplatform.open.common.enums.MaskType;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.vo.model.KeyValue;
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
    private String dataSourceType;

    @Size(max = 255)
    private String url;

    @Size(max = 128)
    private String driver;

    @Size(max = 32)
    private String username;

    @Mask(type = MaskType.PASSWORD)
    @Size(max = 128)
    private String password;

    /**
     * 启用,禁用
     *
     * @see Status
     */
    @NotEmpty
    @Pattern(regexp = "^(ENABLE|DISABLE)$")
    private String status;

    /**
     * 分表规则
     */
    private String partitioningAlgorithm;

    private String healthCheck;

    /**
     * 连接配置
     */
    private List<KeyValue> properties;

    private String description;

}
