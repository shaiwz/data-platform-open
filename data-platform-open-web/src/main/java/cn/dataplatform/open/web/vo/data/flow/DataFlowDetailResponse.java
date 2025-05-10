package cn.dataplatform.open.web.vo.data.flow;

import cn.dataplatform.open.common.enums.Status;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@Data
public class DataFlowDetailResponse {

    private Long id;

    private String name;

    private String code;

    /**
     * 启用,禁用
     *
     * @see Status
     */
    private String status;

    /**
     * 描述
     */
    private String description;

    private Object design;

    private String currentVersion;

    private String publishVersion;

    private String enableAlarm;

    private String enableMonitor;

    /**
     * 运行策略
     * <p>
     * ALL_INSTANCES 全部实例
     * SPECIFY_INSTANCES 指定实例
     * FIXED_INSTANCE_NUMBER 固定实例数
     */
    private String runStrategy;

    /**
     * 运行实例数量
     */
    private Integer instanceNumber;

    /**
     * 指定实例
     */
    private Object specifyInstances;

    private String icon;

    private Long createUserId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
