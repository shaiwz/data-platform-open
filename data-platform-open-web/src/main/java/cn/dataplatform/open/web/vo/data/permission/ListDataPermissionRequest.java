package cn.dataplatform.open.web.vo.data.permission;

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
public class ListDataPermissionRequest {

    private String recordType;

    private Long recordId;

    private String username;

}
