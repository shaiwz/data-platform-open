package cn.dataplatform.open.common.body;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/14
 * @since 1.0.0
 */
@Data
public class DataFlowComponentMessageBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    private String workspaceCode;

    private String flowCode;

    private String componentCode;

    private Type type;


    public enum Type {
        /**
         * 加载,以及移除
         */
        START, STOP, RESTART
    }


}
