package cn.dataplatform.open.web.vo.role;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleAddRequest {

    @NotEmpty
    @Size(min = 1, max = 64)
    private String name;

//    @NotEmpty
//    @Size(min = 1, max = 64)
//    @Pattern(regexp = "^[0-9A-Za-z_:-]{1,32}$")
//    private String code;

    @NotEmpty
    @Size(min = 1, max = 64)
    @Pattern(regexp = "^(ENABLE|DISABLE)$")
    private String status;

}
