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
public class PermissionTransferRequest {

    @NotNull
    private Integer workspaceId;

    @NotNull
    private Integer userId;

    /**
     * 1设置为管理 0设置为普通用户
     */
    @NotNull
    private Integer type;

}
