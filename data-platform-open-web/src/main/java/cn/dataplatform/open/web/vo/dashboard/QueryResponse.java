
package cn.dataplatform.open.web.vo.dashboard;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class QueryResponse {

    public static final String SUCCESS = "success";

    private String status;
    private Data data;

    @lombok.Data
    public static class Data {
        public static final String MATRIX = "matrix";
        public static final String VECTOR = "vector";
        public static final String SCALAR = "scalar";

        private String resultType;
        private String result;

        public List<? extends Body> getResult() {
            if (result == null) {
                return Collections.emptyList();
            }
            if (resultType.equals(MATRIX)) {
                return JSON.parseArray(result, MatrixBody.class);
            } else if (resultType.equals(VECTOR)) {
                return JSON.parseArray(result, VectorBody.class);
            }
            return JSON.parseArray(result, OtherBody.class);
        }

        public List<VectorBody> getVectorBody() {
            if (result == null) {
                return Collections.emptyList();
            }
            return JSON.parseArray(result, VectorBody.class);
        }

        public VectorBody getVectorBodyFast() {
            if (result == null) {
                return new VectorBody();
            }
            List<VectorBody> vectorBodies = JSON.parseArray(result, VectorBody.class);
            if (vectorBodies.isEmpty()) {
                return new VectorBody();
            }
            return vectorBodies.getFirst();
        }

        public List<MatrixBody> getMatrixBody() {
            if (result == null) {
                return Collections.emptyList();
            }
            return JSON.parseArray(result, MatrixBody.class);
        }

        public MatrixBody getMatrixBodyFast() {
            if (result == null) {
                return new MatrixBody();
            }
            List<MatrixBody> matrixBodies = JSON.parseArray(result, MatrixBody.class);
            if (matrixBodies.isEmpty()) {
                return new MatrixBody();
            }
            return matrixBodies.getFirst();
        }

    }

    public interface Body {
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    public static class OtherBody extends LinkedHashMap<String, Object> implements Body {
    }

    /**
     * metric
     */
    @lombok.Data
    public static class VectorBody implements Body {
        /**
         * vector
         */
        private List<Object> value;

        /**
         * 获取第一个值
         */
        public Object getFirst() {
            if (value == null || value.isEmpty()) {
                return null;
            }
            return value.getFirst();
        }

        /**
         * 获取最后一个值
         */
        public Object getLast() {
            if (value == null || value.isEmpty()) {
                return null;
            }
            return value.getLast();
        }

    }

    @lombok.Data
    public static class MatrixBody implements Body {

        private Map<String, Object> metric;
        private List<List<Object>> values;
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return !SUCCESS.equals(status);
    }

}