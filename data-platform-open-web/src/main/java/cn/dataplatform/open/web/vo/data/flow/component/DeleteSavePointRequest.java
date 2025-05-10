package cn.dataplatform.open.web.vo.data.flow.component;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/16
 * @since 1.0.0
 */
@Data
public class DeleteSavePointRequest {


    @NotNull
    private Long flowId;

    @NotNull
    private Long savePointId;

}
