package cn.dataplatform.open.web.vo.user;


import cn.dataplatform.open.common.annotation.Mask;
import cn.dataplatform.open.common.enums.MaskType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserAddRequest {

    @NotEmpty
    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[0-9A-Za-z_-]{4,32}$")
    private String username;

    @Mask(type = MaskType.PASSWORD)
    @NotEmpty
    @Size(min = 3, max = 32)
    private String password;

    @Mask(type = MaskType.PHONE)
    private String phone;

    @NotNull
    @Email
    private String email;

    private String avatar;

    @NotEmpty
    @Size(min = 1, max = 64)
    @Pattern(regexp = "^(ENABLE|DISABLE)$")
    private String status;

}
