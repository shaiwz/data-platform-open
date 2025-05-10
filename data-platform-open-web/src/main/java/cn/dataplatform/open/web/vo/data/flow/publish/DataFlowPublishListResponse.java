package cn.dataplatform.open.web.vo.data.flow.publish;

import cn.dataplatform.open.common.enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/19
 * @since 1.0.0
 */
@Data
public class DataFlowPublishListResponse {

    private Long id;

    private String name;

    private String code;
    private Long workspaceId;

    /**
     * 启用,禁用
     *
     * @see Status
     */
    private String status;
    private String publishDescription;
    /**
     * 描述
     */
    private String description;
    private String icon;

    private String version;

    private Long createUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Long flowId;

}
