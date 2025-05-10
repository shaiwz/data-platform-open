package cn.dataplatform.open.web.vo.workspace;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/18
 * @since 1.0.0
 */
@Data
public class NotInMembersRequest {

    /**
     * 用户名称模糊查询
     */
    private String username;

    @NotNull
    private Long workspaceId;

}
