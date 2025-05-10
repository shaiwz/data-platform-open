package cn.dataplatform.open.common.vo.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;

/**
 * 〈返回分页数据〉<br>
 *
 * @author 丁乾文
 * @create 2019/9/30
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> extends BaseResult {

    @Serial
    private static final long serialVersionUID = -6689010616909835472L;

    /**
     * 分页数据
     */
    private Data<T> data;

    public PageResult(List<T> records, Long current, Long size, Long total) {
        this.data = new Data<>(records, current, size, total);
    }

    public void setData(List<T> collect, long current, long size, long total) {
        this.data = new Data<>(collect, current, size, total);
    }


    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data<T> extends PageBase {

        /**
         * 分页数据
         */
        private List<T> records;

        /**
         * 总记录数
         */
        private Long total;

        /**
         * 总页数
         */
        public Data(List<T> records, Long current, Long size, Long total) {
            this.records = records;
            this.current = current;
            this.size = size;
            this.total = total;
        }

    }

}
