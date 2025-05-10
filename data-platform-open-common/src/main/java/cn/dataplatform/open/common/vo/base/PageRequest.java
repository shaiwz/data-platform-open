package cn.dataplatform.open.common.vo.base;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 丁乾文
 * @create 2019/9/30
 * @since 1.0.0
 */
@Data
public class PageRequest<T> {

    /**
     * 查询条件
     */
    @NotNull
    @Valid
    private T query;

    /**
     * 分页参数
     */
    @NotNull
    private PageBase page = new PageBase();

    /**
     * 排序条件
     */
    @Valid
    private List<OrderBy> orders = new ArrayList<>();

    @Data
    public static class OrderBy {
        /**
         * 排序列名
         */
        @NotBlank
        private String columnName;
        /**
         * 是否降序
         */
        private boolean desc;

    }

}
