package cn.dataplatform.open.web.vo.data.source.console;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/25
 * @since 1.0.0
 */
@Data
public class SendMQMessageRequest {

    @NotNull
    public Long id;


    /**
     * 主题
     */
    @NotBlank
    private String topic;

    /**
     * 是否自动创建队列
     */
    private Boolean autoCreateQueue = false;

    private Map<String, String> headers;

    @NotBlank
    private String message;

    private String group;

    private String tag;

    private String key;

    private Integer partition;

}
