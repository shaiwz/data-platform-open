package cn.dataplatform.open.web.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDetailResponse {

    private Long id;

    private String username;

    private String email;

    private String phone;

    private String avatar;

    /**
     * 性别
     */
    private String gender;

    private Long createUserId;

    private String status;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
