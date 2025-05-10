
package cn.dataplatform.open.web.vo.data.flow.log;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QueryResponse {

    public static final String SUCCESS = "success";

    private String status;
    private Data data;

    @lombok.Data
    public static class Data {

        private String resultType;
        private List<Result> result;

    }

    @lombok.Data
    public static class Result {

        private Map<String, String> stream;

        /**
         * 第一个为时间戳1741439487925029538
         * 第二个为日志详情
         */
        private List<List<String>> values;

    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return !SUCCESS.equals(status);
    }

}