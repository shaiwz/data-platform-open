package cn.dataplatform.open.flow.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/19
 * @since 1.0.0
 */
@Slf4j
public class BatchUtils {

    /**
     * 自动调整查询条目
     *
     * @return 查询条目
     */
    public static long autoSize() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsageRatio = (double) usedMemory / totalMemory * 100;
        log.info("当前内存使用情况:总内存:{}M,使用率:{}", totalMemory / 1024 / 1024, String.format("%.2f", memoryUsageRatio));
        long size;
        if (memoryUsageRatio > 90) {
            size = 1000;
        } else if (memoryUsageRatio > 80) {
            size = 2000;
        } else if (memoryUsageRatio > 70) {
            size = 5000;
        } else if (memoryUsageRatio > 60) {
            size = 10000;
        } else {
            size = 20000;
        }
        log.info("设置查询条目:{}", size);
        return size;
    }

}
