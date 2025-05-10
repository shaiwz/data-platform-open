package cn.dataplatform.open.web.vo.user;


import cn.dataplatform.open.common.annotation.Mask;
import cn.dataplatform.open.common.enums.MaskType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotNull
    private Long id;

    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[0-9A-Za-z_-]{3,32}$")
    private String password;

    @Mask(type = MaskType.EMAIL)
    @Email
    private String email;

    @Mask(type = MaskType.PHONE)
    private String phone;

    private String avatar;

    @Size(min = 1, max = 64)
    @Pattern(regexp = "^(ENABLE|DISABLE)$")
    private String status;

    /**
     * 性别
     */
    private String gender;

}
