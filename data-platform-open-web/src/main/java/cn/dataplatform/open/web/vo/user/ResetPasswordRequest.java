package cn.dataplatform.open.web.vo.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotNull
    private Long id;

    @NotEmpty
    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[0-9A-Za-z_-]{3,32}$")
    private String password;

}
