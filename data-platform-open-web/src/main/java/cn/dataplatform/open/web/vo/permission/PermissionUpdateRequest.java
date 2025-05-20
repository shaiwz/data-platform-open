package cn.dataplatform.open.web.vo.permission;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PermissionUpdateRequest extends PermissionAddRequest {

    @NotNull
    private Long id;

}
