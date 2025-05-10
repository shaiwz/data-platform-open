package cn.dataplatform.open.web.vo.user;


import cn.dataplatform.open.common.annotation.Mask;
import cn.dataplatform.open.common.enums.MaskType;
import lombok.Data;

@Data
public class UserListRequest {

    private String username;

    @Mask(type = MaskType.EMAIL)
    private String email;

    private String avatar;

    private String status;

}
