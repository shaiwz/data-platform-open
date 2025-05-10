package cn.dataplatform.open.web.vo.user;


import cn.dataplatform.open.common.annotation.Mask;
import cn.dataplatform.open.common.enums.MaskType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserListResponse {

    private Long id;

    private String username;

    @Mask(type = MaskType.EMAIL)
    private String email;

    @Mask(type = MaskType.PHONE)
    private String phone;

    private String avatar;

    private Long createUserId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private String status;

    /**
     * 是否还在线
     */
    private Boolean online;

}
