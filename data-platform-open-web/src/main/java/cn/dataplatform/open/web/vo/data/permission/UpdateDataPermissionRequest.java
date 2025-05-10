package cn.dataplatform.open.web.vo.data.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/17
 * @since 1.0.0
 */
@Data
public class UpdateDataPermissionRequest {

    @NotBlank
    private String recordType;

    @NotNull
    private Long recordId;

    /**
     * 0有写权限
     */
    @NotBlank
    private String writeAuthority;

    /**
     * 0有发布规则权限
     */
    @NotBlank
    private String publishAuthority;

    /**
     * 用户id
     */
    @NotNull
    private Long userId;

}
