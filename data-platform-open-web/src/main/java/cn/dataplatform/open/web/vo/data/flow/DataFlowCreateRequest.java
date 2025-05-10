package cn.dataplatform.open.web.vo.data.flow;

import jakarta.validation.constraints.NotBlank;
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
public class DataFlowCreateRequest {

    @NotBlank
    @Size(max = 20)
    private String name;

    @NotBlank
    private String icon;

    @NotBlank
    private String status;

    private String description;

}
