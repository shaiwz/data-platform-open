package cn.dataplatform.open.common.enums.flow;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/25
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum DataFlowRunStrategy {


    /**
     * 运行策略
     * <p>
     * ALL_INSTANCES 全部实例
     * SPECIFY_INSTANCES 指定实例
     * FIXED_INSTANCE_NUMBER 固定实例数
     */
    ALL_INSTANCES("全部实例"),
    SPECIFY_INSTANCES("指定实例"),
    FIXED_INSTANCE_NUMBER("固定实例数");

    private final String name;

}
