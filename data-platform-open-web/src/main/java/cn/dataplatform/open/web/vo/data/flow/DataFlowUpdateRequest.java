package cn.dataplatform.open.web.vo.data.flow;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@Data
public class DataFlowUpdateRequest {

    @NotNull
    private Long id;

    @Size(max = 20)
    private String name;

    private String icon;

    private String status;

    private String description;

    private String design;

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
    private List<String> specifyInstances;

    /**
     * 暂存标识
     */
    private Boolean temporarily;

}
