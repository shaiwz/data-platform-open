package cn.dataplatform.open.common.vo.base;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class IdRequest {


    @NotNull(message = "Id不能为空")
    private Long id;
}
