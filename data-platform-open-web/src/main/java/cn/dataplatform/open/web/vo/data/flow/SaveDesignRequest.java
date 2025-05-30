package cn.dataplatform.open.web.vo.data.flow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/11
 * @since 1.0.0
 */
@Data
public class SaveDesignRequest {

    @NotNull
    private Long id;

    @NotBlank
    private String design;

}
