package cn.dataplatform.open.web.vo.data.flow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/19
 * @since 1.0.0
 */
@Data
public class PublishRequest {

    @NotNull
    private Long id;

    /**
     * 发布描述
     */
    @NotBlank
    private String publishDescription;

}
