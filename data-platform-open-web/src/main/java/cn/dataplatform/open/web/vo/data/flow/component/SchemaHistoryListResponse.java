package cn.dataplatform.open.web.vo.data.flow.component;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/25
 * @since 1.0.0
 */
@Data
public class SchemaHistoryListResponse {

    private Long id;

    private String workspaceCode;

    private String flowCode;

    private String componentCode;

    private String instanceId;


    private String schemaLine;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
