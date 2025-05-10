package cn.dataplatform.open.common.body;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DataFlowMessageBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Type type;

    private Long id;
    private String workspaceCode;


    public enum Type {
        /**
         * 加载,以及移除
         */
        LOAD, UPDATE, REMOVE
    }

}