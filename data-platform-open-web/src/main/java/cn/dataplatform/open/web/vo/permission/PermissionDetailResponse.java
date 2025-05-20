package cn.dataplatform.open.web.vo.permission;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PermissionDetailResponse {

    private Long id;

    private String name;

    private String code;

    private Long createUserId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String status;

}
