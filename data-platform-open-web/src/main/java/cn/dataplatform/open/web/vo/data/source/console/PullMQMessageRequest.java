package cn.dataplatform.open.web.vo.data.source.console;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/25
 * @since 1.0.0
 */
@Data
public class PullMQMessageRequest {

    @NotNull
    public Long id;

    /**
     * 主题
     */
    @NotBlank
    private String topic;

    /**
     * 拉取的数量
     */
    @Max(1000)
    private Integer pullNum;

    /**
     * 是否ack
     */
    private Boolean ack = false;

}
