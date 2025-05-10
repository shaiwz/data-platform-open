package cn.dataplatform.open.web.vo.login.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/9
 * @since 1.0.0
 */
@Data
public class LoginLogDetailResponse {

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
