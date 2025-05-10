package cn.dataplatform.open.web.vo.data.permission;

import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/17
 * @since 1.0.0
 */
@Data
public class ListDataPermissionResponse {

    private String username;

    private String email;

    private String avatar;

    private Long userId;


    /**
     * 写权限
     */
    private String writeAuthority;

    /**
     * 发布规则权限
     */
    private String publishAuthority;


}
