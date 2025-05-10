package cn.dataplatform.open.web.vo.role;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRoleUpsertRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long roleId;

    @NotEmpty
    @Size(min = 1, max = 64)
    @Pattern(regexp = "^(ENABLE|DISABLE)$")
    private String status;

}
