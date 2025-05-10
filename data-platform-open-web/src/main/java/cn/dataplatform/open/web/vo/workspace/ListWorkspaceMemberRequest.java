package cn.dataplatform.open.web.vo.workspace;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ListWorkspaceMemberRequest {

    /**
     * 查询管理员还是普通用户
     */
    @NotNull
    private Integer type;

    /**
     * 用户名称模糊查询
     */
    private String username;

    @NotNull
    private Long workspaceId;

}