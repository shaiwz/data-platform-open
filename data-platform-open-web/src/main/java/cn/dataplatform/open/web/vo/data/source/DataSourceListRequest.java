package cn.dataplatform.open.web.vo.data.source;

import lombok.Data;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@Data
public class DataSourceListRequest {

    private String name;

    private String code;

    private String type;

    private List<String> types;

    private String status;
}
