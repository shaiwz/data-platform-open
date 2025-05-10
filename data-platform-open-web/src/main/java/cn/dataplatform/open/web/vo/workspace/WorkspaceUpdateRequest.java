package cn.dataplatform.open.web.vo.workspace;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkspaceUpdateRequest {

    @NotNull
    private Long id;

    @Size(min = 1, max = 64)
    private String name;

    @Size(min = 1, max = 64)
    private String secret;

    @Size(min = 1, max = 64)
    @Pattern(regexp = "^(ENABLE|DISABLE)$")
    private String status;

}
