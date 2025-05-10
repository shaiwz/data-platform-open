package cn.dataplatform.open.web.store.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/1
 * @since 1.0.0
 */
@Data
@TableName("user_login_log")
public class UserLoginLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String requestId;

    private Long userId;
    private String username;

    private String ip;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 系统
     */
    private String os;

    /**
     * 详细信息
     */
    private String userAgent;

    private String platform;

    private LocalDateTime createTime;

}
