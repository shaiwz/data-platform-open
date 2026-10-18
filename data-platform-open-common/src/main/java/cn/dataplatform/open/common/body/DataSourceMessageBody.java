package cn.dataplatform.open.common.body;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DataSourceMessageBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Type type;

    private String workspaceCode;

    private String dataSourceCode;


    public enum Type {
        /**
         * 加载,以及移除
         */
        LOAD, UPDATE, REMOVE
    }

}