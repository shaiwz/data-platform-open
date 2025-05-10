package cn.dataplatform.open.web.vo.data.source;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DataSourceUpdateRequest extends DataSourceAddRequest {

    @NotNull
    private Long id;

}
