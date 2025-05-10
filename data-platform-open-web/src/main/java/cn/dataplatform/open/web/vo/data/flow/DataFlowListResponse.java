package cn.dataplatform.open.web.vo.data.flow;

import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.vo.flow.FlowError;
import cn.dataplatform.open.common.vo.flow.FlowHeartbeat;
import cn.dataplatform.open.web.vo.user.UserData;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@Data
public class DataFlowListResponse {

    private Long id;

    private String name;

    private String code;

    /**
     * 启用,禁用
     *
     * @see Status
     */
    private String status;

    /**
     * 描述
     */
    private String description;

    private String icon;

    private String currentVersion;

    private String publishVersion;

    /**
     * 已发布表中的的数据流ID
     */
    private Long publishId;

    private Long createUserId;

    /**
     * 编辑过的用户,创建人放在首位
     */
    private List<UserData> users = Collections.emptyList();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private List<FlowError> flowErrors;

    private Collection<FlowHeartbeat> flowHeartbeats;

}
