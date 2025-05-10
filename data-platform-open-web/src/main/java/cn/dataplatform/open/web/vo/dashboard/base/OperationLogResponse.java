package cn.dataplatform.open.web.vo.dashboard.base;

import cn.dataplatform.open.web.vo.user.UserData;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

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
public class OperationLogResponse {

    private Long id;

    private String function;

    private String action;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private UserData user;

}
