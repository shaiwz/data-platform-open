package cn.dataplatform.open.web.vo.workspace;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkspaceAddRequest {

    @Size(min = 1, max = 64)
    private String name;

    @Size(min = 1, max = 64)
    @Pattern(regexp = "^[0-9A-Za-z_-]{1,64}$")
    private String code;

    @Size(min = 1, max = 64)
    @Pattern(regexp = "^[0-9A-Za-z_-]{1,64}$")
    private String secret;

    @NotEmpty
    @Size(min = 1, max = 64)
    @Pattern(regexp = "^(ENABLE|DISABLE)$")
    private String status;

}
