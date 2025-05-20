package cn.dataplatform.open.web.vo.permission;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionUpsertRequest {

    @NotNull
    private Long roleId;

    /**
     * 选中的
     */
    @NotNull
    private List<Long> permissionIds;

}
