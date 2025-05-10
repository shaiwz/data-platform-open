package cn.dataplatform.open.web.vo.data.source.console;

import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/16
 * @since 1.0.0
 */
@Data
public class ExecuteElasticResponse {

    /**
     * json
     */
    private Object result;

    /**
     * 耗时ms
     */
    private long cost;

}
