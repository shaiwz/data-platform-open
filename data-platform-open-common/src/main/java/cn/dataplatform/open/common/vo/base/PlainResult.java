package cn.dataplatform.open.common.vo.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 〈返回普通数据〉<br>
 *
 * @author 丁乾文
 * @create 2019/9/30
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class PlainResult<T> extends BaseResult {

    @Serial
    private static final long serialVersionUID = 8794822903345524683L;

    /**
     * 数据
     */
    private T data;

}
