package cn.dataplatform.open.common.server;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/28
 * @since 1.0.0
 */
@Data
public class Server implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String host;

    private Integer port;

    /**
     * 首次注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fastHeartbeat;

    /**
     * 当前服务启动时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latelyStartTime;

    /**
     * 最近一次心跳时间
     * <p>
     * 查询时,只查询健康的,存活时间在30s内的
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastHeartbeat;

    /**
     * 服务状态
     */
    private ServerStatus status;
    /**
     * 总jvm内存 单位G，2位小数
     */
    private BigDecimal totalMemory;
    /**
     * 已使用jvm内存 单位G，2位小数
     */
    private BigDecimal freeMemory;
    /**
     * cpu占用率
     */
    private BigDecimal cpuUsageRatio;

    /**
     * 判断服务是否存活
     *
     * @return 如果服务在最近30秒内有过心跳, 则返回 ONLINE；否则返回 OFFLINE
     */
    public ServerStatus getStatus() {
        if (status != null) {
            return status;
        }
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 判断最近一次心跳时间与当前时间的差是否小于30秒
        if (lastHeartbeat == null) {
            return ServerStatus.OFFLINE;
        }
        return Duration.between(lastHeartbeat, now).getSeconds() < 30 ? ServerStatus.ONLINE : ServerStatus.INACTIVE;
    }


    /**
     * 获取服务实例ID
     *
     * @return instanceId
     */
    public String getInstanceId() {
        return host + ":" + port;
    }

    /**
     * 获取内存使用率
     *
     * @return 内存使用率
     */
    public BigDecimal getMemoryUsageRatio() {
        if (totalMemory == null || totalMemory.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (freeMemory == null) {
            freeMemory = BigDecimal.ZERO;
        }
        return freeMemory.divide(totalMemory, 2, RoundingMode.HALF_UP);
    }

}
