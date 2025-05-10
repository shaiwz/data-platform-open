package cn.dataplatform.open.web.vo.data.flow.component;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/16
 * @since 1.0.0
 */
@Data
public class SavePointListResponse {

    private Long id;

    private String flowCode;

    private String componentCode;

    private String instanceId;

    /**
     * 保存点唯一编码
     */
    private String savePoint;

    private String key;

    private String value;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 到期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

}
