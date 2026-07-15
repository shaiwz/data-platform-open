package cn.dataplatform.open.common.server;

import cn.dataplatform.open.common.enums.ServerStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

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

    private String contextPath;

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
     * -Xmx 设置的最大上限 单位G，2位小数
     */
    private BigDecimal maxMemory;
    /**
     * 当前 JVM 已从操作系统申请到的内存 单位G，2位小数
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
     * @return 如果服务在最近60秒内有过心跳, 则返回 ONLINE；否则返回 OFFLINE
     */
    public ServerStatus getStatus() {
        // 已经不是在线状态，直接返回即可，否则后续再判断心跳状态
        if (Objects.equals(this.status, ServerStatus.OFFLINE)) {
            return ServerStatus.OFFLINE;
        }
        if (this.lastHeartbeat == null) {
            return ServerStatus.OFFLINE;
        }
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 判断最近一次心跳时间与当前时间的差是否小于n秒
        return Duration.between(this.lastHeartbeat, now).getSeconds() < 60 ? ServerStatus.ONLINE : ServerStatus.INACTIVE;
    }


    /**
     * 获取服务实例ID
     *
     * @return instanceId
     */
    public String getInstanceId() {
        return this.host + ":" + this.port;
    }

    /**
     * 获取内存使用率
     * <p>
     * 计算公式：(totalMemory - freeMemory) / maxMemory
     *
     * @return 内存使用率 (例如 75.25 代表 75.25%)
     */
    public BigDecimal getMemoryUsageRatio() {
        if (this.maxMemory == null || this.maxMemory.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal currentFree = this.freeMemory == null ? BigDecimal.ZERO : this.freeMemory;
        BigDecimal currentTotal = this.totalMemory == null ? BigDecimal.ZERO : this.totalMemory;

        BigDecimal usedMemory = currentTotal.subtract(currentFree);
        return usedMemory.multiply(new BigDecimal(100))
                .divide(this.maxMemory, 2, RoundingMode.HALF_UP);
    }

}
