package cn.dataplatform.open.common.vo.base;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 〈公用参数〉<br>
 *
 * @author 丁乾文
 * @create 2019/9/30
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Param<T> {

    /**
     * 公用参数字段
     */
    @NotNull(message = "参数不能为空")
    private T param;
}
