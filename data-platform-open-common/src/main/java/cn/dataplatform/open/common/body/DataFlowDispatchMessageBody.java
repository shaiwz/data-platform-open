package cn.dataplatform.open.common.body;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class DataFlowDispatchMessageBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Type type;

    private String flowCode;
    private String workspaceCode;

    /**
     * 调度的实例
     */
    private List<String> instanceIds;


    public enum Type {
        /**
         * 运行，停止
         */
        START, STOP
    }

}