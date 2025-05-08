package cn.dataplatform.open.common.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/25
 * @since 1.0.0
 */
public class JsonJacksonCodec extends org.redisson.codec.JsonJacksonCodec {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper() {{
        this.registerModule(new JavaTimeModule());
        this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }};

    public JsonJacksonCodec() {
        super(OBJECT_MAPPER, false);
    }

}
