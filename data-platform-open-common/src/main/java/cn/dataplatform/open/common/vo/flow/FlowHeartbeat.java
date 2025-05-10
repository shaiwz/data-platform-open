package cn.dataplatform.open.common.vo.flow;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/9
 * @since 1.0.0
 */
@Data
public class FlowHeartbeat implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String instanceId;

    /**
     * 首次注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fastHeartbeat;

    /**
     * 最近一次心跳时间
     * <p>
     * 查询时,只查询健康的,存活时间在30s内的
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastHeartbeat;

    /**
     * 是否正常
     *
     * @return true:正常, false:异常
     */
    public boolean isNormal() {
        return lastHeartbeat != null && lastHeartbeat.isAfter(LocalDateTime.now().minusSeconds(30));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowHeartbeat that = (FlowHeartbeat) o;
        return Objects.equals(instanceId, that.instanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceId);
    }
}
