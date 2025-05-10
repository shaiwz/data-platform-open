package cn.dataplatform.open.web.vo.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotEmpty
    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[0-9A-Za-z_-]{3,32}$")
    private String oldPassword;

    @NotEmpty
    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[0-9A-Za-z_-]{3,32}$")
    private String newPassword;

}
