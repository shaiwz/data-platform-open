package cn.dataplatform.open.common.server;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/25
 * @since 1.0.0
 */
@Component
public class ServerMonitor {

    /**
     * 获取JVM CPU使用率
     *
     * @return JVM CPU使用率百分比
     */
    public BigDecimal getJvmCpuUsage() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpuUsage = osBean.getProcessCpuLoad() * 100;
        // 处理第一次调用可能返回-1的情况
        if (cpuUsage < 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(cpuUsage).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取JVM最大可用内存(Xmx)
     *
     * @return JVM最大内存(GB)
     */
    public BigDecimal getJvmMaxMemory() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        return BigDecimal.valueOf(maxMemory / (1024.0 * 1024.0 * 1024.0))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取JVM已分配内存
     *
     * @return JVM已分配内存(GB)
     */
    public BigDecimal getJvmTotalMemory() {
        long totalMemory = Runtime.getRuntime().totalMemory();
        return BigDecimal.valueOf(totalMemory / (1024.0 * 1024.0 * 1024.0))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取JVM空闲内存
     *
     * @return JVM空闲内存(GB)
     */
    public BigDecimal getJvmFreeMemory() {
        long freeMemory = Runtime.getRuntime().freeMemory();
        return BigDecimal.valueOf(freeMemory / (1024.0 * 1024.0 * 1024.0))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取JVM已使用内存
     *
     * @return JVM已使用内存(GB)
     */
    public BigDecimal getJvmUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return BigDecimal.valueOf(usedMemory / (1024.0 * 1024.0 * 1024.0))
                .setScale(2, RoundingMode.HALF_UP);
    }

}
