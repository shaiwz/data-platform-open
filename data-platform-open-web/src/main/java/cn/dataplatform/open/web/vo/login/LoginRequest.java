package cn.dataplatform.open.web.vo.login;


import cn.dataplatform.open.common.annotation.Mask;
import cn.dataplatform.open.common.enums.MaskType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 获取验证码请求参数
 *
 * @author DaoDao
 */
@Data
public class LoginRequest {

    /**
     * 用户名/邮箱
     */
    @NotEmpty
    @Size(min = 4, max = 32)
    private String account;

    /**
     * 密码
     */
    @Mask(type = MaskType.PASSWORD)
    @NotEmpty
    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[0-9A-Za-z_-]{3,32}$")
    private String password;

}
