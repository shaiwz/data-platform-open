package cn.dataplatform.open.web.vo.data.source;

import cn.dataplatform.open.common.enums.DataSourceType;
import cn.dataplatform.open.common.enums.Status;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@Data
public class DataSourceTestRequest {


    private Long id;

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

    @Size(min = 1, max = 255)
    private String url;

    @Size(min = 1, max = 128)
    private String driver;

    private String username;

    private String password;

    /**
     * 启用,禁用
     *
     * @see Status
     */
    @NotEmpty
    @Pattern(regexp = "^(ENABLE|DISABLE)$")
    private String status;

}
