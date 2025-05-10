package cn.dataplatform.open.common.body;

import cn.dataplatform.open.common.alarm.scene.Scene;
import cn.dataplatform.open.common.server.ServerManager;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/22
 * @since 1.0.0
 */
@NoArgsConstructor
@Data
public class AlarmSceneMessageBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String serverName;
    @NotBlank
    private String instanceId;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @NotNull
    private Scene scene;
    @NotBlank
    private String workspaceCode;
    /**
     * 告警时间
     */
    @NotNull
    private LocalDateTime alarmTime = LocalDateTime.now();


    /**
     * 场景编码
     */
    public AlarmSceneMessageBody(Scene scene) {
        this.scene = scene;
    }

    /**
     * 获取服务名称
     *
     * @return 服务名称
     */
    public String getServerName() {
        if (serverName != null) {
            return serverName;
        }
        ServerManager serverManager = SpringUtil.getBean(ServerManager.class);
        return serverManager.getApplicationName();
    }

    /**
     * 获取实例ID
     *
     * @return 实例ID
     */
    public String getInstanceId() {
        if (instanceId != null) {
            return instanceId;
        }
        ServerManager serverManager = SpringUtil.getBean(ServerManager.class);
        return serverManager.instanceId();
    }

}
