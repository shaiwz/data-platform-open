package cn.dataplatform.open.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/1
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum OperationLogFunction {

    /**
     * 数据源
     */
    DATASOURCE("数据源"),
    /**
     * 数据流
     */
    DATA_FLOW("数据流"),
    /**
     * 查询模板
     */
    QUERY_TEMPLATE("查询模板"),
    /**
     * 数据对齐
     */
    DATA_ALIGN("数据对齐"),
    /**
     * 文件
     */
    FILE("文件"),
    ;

    private final String name;


}
