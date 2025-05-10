package cn.dataplatform.open.web.vo.data.source;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/4
 * @since 1.0.0
 */
@Data
public class SchemaTableMap {

    private String key;
    private String label;
    private String tag;

    private List<Children> children = new ArrayList<>();

    public void addChildren(Children children) {
        this.children.add(children);
    }

    /**
     * 表信息
     */
    @Data
    public static class Children {
        /**
         * 库
         */
        private String schema;
        /**
         * 表名
         */
        private String key;
        /**
         * 表名（备注）
         */
        private String label;
    }


}
