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
public class BindMemberRequest {

    /**
     * 绑定的用户列表
     */
    @NotNull
    private Long userId;

    @NotNull
    private Long workspaceId;

}
