package cn.dataplatform.open.web.vo.workspace;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class WorkspaceData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String code;

    private String secret;

    /**
     * 是否是工作空间管理员
     */
    private Boolean isWorkspaceAdmin;

}
