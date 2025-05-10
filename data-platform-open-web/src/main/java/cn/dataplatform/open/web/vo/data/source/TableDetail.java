package cn.dataplatform.open.web.vo.data.source;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/27
 * @since 1.0.0
 */
@Data
public class TableDetail {

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String comment;

    /**
     * 列信息
     */
    private List<Column> columns;

    private List<Index> indexes;


    @Data
    public static class Column {
        /**
         * 列名
         */
        private String name;
        /**
         * 列备注
         */
        private String comment;
        /**
         * 列类型
         */
        private String type;

        /**
         * 是否主键
         */
        private boolean primaryKey;

        /**
         * 是否必填
         */
        private boolean notNull;

        private String defaultValue;

        private Long maxLength;
    }

    @Data
    public static class Index {

        private String name;
        private Boolean unique;

        private List<String> columns = new ArrayList<>();

        /**
         * add
         */
        public void addColumn(String column) {
            columns.add(column);
        }

    }

}
