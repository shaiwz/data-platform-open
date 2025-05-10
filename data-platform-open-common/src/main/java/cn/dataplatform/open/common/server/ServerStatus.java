package cn.dataplatform.open.common.server;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/23
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ServerStatus {
    /**
     * 最近30秒有心跳
     */
    ONLINE("在线"),
    /**
     * 服务主动中断,或者手动调用接口让服务下线,不在接收并处理数据流请求
     */
    OFFLINE("下线"),
    /**
     * 没有心跳检测
     */
    INACTIVE("失活");
    private final String name;

}
