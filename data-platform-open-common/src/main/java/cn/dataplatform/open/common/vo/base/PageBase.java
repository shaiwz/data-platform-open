package cn.dataplatform.open.common.vo.base;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 丁乾文
 * @create 2019/9/30
 * @since 1.0.0
 */
@Data
public class PageBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    protected long current = 1;

    /**
     * 每页数量
     */
    protected long size = 10;

}
