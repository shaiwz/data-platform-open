package cn.dataplatform.open.web.vo.data.source;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/27
 * @since 1.0.0
 */
@Data
public class TableDetailRequest {

    @NotNull
    private Long id;

    @NotBlank
    private String schema;

    @NotBlank
    private String table;

}
