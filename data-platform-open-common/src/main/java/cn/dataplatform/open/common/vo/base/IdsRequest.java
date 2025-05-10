package cn.dataplatform.open.common.vo.base;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 〈Id查询请求〉<br>
 * 〈〉
 *
 * @author 丁乾文
 * @create 2019/9/30
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdsRequest {


    @NotNull(message = "Ids不能为空")
    private List<Long> ids;
}
