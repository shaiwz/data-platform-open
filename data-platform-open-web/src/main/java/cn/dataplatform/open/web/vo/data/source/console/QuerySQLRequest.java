package cn.dataplatform.open.web.vo.data.source.console;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/4
 * @since 1.0.0
 */
@Data
public class QuerySQLRequest {

    @NotNull
    public Long id;

    /**
     * 查询哪个库
     * 如果SQL里面没有指定,使用此库
     */
    @Size(min = 1, max = 64)
    private String schema;

    @NotEmpty
    @Size(min = 1, max = 10240)
    private String sql;

}
