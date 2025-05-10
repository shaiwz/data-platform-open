package cn.dataplatform.open.web.vo.data.flow;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/21
 * @since 1.0.0
 */
@Data
public class ListPublishedResponse {

    private Long id;

    private String code;

    private String name;


    private List<Component> components = Collections.emptyList();


    @Data
    public static class Component {
        private String id;
        private String type;
        private String name;
    }

}
