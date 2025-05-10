package cn.dataplatform.open.common.alarm.scene;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 服务下线通知
 *
 * @author dingqianwen
 * @date 2025/2/23
 * @since 1.0.0
 */
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Data
public class ServiceOfflineNoticeScene implements Scene {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String scene() {
        return "SERVICE_OFFLINE_NOTICE";
    }

}
