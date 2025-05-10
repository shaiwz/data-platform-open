package cn.dataplatform.open.web.vo.data.source.console;

import cn.dataplatform.open.web.vo.data.source.Column;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/4
 * @since 1.0.0
 */
@Data
public class QuerySQLResponse {

    private List<Column> column = new ArrayList<>();

    private List<Map<String, Object>> rows = new ArrayList<>();

    /**
     * 耗时ms
     */
    private long cost;

    /**
     * add
     */
    public void addColumn(Column column) {
        this.column.add(column);
    }

    public void addRow(Map<String, Object> row) {
        this.rows.add(row);
    }

}
