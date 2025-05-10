package cn.dataplatform.open.web.vo.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/19
 * @since 1.0.0
 */
@Data
public class UserData implements Serializable {

    @Serial
    private static final long serialVersionUID = -5944149026431724373L;

    /**
     * admin
     */
    public static final String ADMIN = "admin";

    private Long id;

    private String username;

    private String email;

    private String avatar;

    private String status;

    /**
     * 性别
     */
    private String gender;

    private String phone;

    private String description;

    /**
     * 是否为超级管理员
     */
    public boolean isAdmin() {
        return ADMIN.equals(username);
    }

}
