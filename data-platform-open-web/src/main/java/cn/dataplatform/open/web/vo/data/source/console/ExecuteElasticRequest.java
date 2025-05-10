package cn.dataplatform.open.web.vo.data.source.console;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ExecuteElasticRequest {

    @NotNull
    public Long id;

    /**
     * <pre>
     * GET /_search
     * {
     *   "query": {
     *     "match_all": {}
     *   }
     * }
     * </pre>
     */
    @NotBlank
    private String script;

}
