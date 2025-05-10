package cn.dataplatform.open.common.body;

import cn.dataplatform.open.common.server.ServerManager;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/22
 * @since 1.0.0
 */
@Data
public class AlarmMessageBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String serverName;
    @NotBlank
    private String instanceId;
    /**
     * 机器人编码
     */
    private String robotCode;
    /**
     * 模板编码
     */
    private String templateCode;
    @NotBlank
    private String workspaceCode;
    /**
     * 不需要打印参数
     */
    @NotNull
    @JSONField(serialize = false)
    private Map<String, Object> parameter;
    /**
     * 告警时间
     */
    @NotNull
    private LocalDateTime alarmTime = LocalDateTime.now();


    /**
     * 设置参数
     *
     * @param parameter 参数
     */
    public void setParameter(Object parameter) {
        this.parameter = BeanUtil.beanToMap(parameter);
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
