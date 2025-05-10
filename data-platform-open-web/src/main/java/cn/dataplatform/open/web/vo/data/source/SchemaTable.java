package cn.dataplatform.open.web.vo.data.source;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/4
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SchemaTable {

    private String schema;

    private String table;

    private String comment;

}
