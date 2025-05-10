package cn.dataplatform.open.web.vo.dashboard.base;

import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/1
 * @since 1.0.0
 */
@Data
public class OperationLogRequest {

    /**
     * 查询数据范围,个人,或者工作空间
     */
    private String scope;

}
