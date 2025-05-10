package cn.dataplatform.open.common.introspect;


import cn.dataplatform.open.common.annotation.Mask;
import cn.dataplatform.open.common.enums.MaskType;
import cn.dataplatform.open.common.util.MaskUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;

/**
 * 〈objectMapper 支持脱敏〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/9/19
 * @see Mask
 * @since 1.0.0
 */
public class MaskJacksonAnnotationIntrospector extends JacksonAnnotationIntrospector {

    @Override
    public Object findSerializer(Annotated a) {
        Mask mask = this._findAnnotation(a, Mask.class);
        if (mask == null) {
            return null;
        }
        return new JsonSerializer<String>() {
            @SneakyThrows
            @Override
            public void serialize(String string, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
                if (StrUtil.isEmpty(string)) {
                    jsonGenerator.writeString(string);
                    return;
                }
                MaskType type = mask.type();
                string = MaskUtil.mask(type, string);
                jsonGenerator.writeString(string);
            }
        };
    }

    @Override
    public boolean isAnnotationBundle(Annotation ann) {
        return Mask.class.isAssignableFrom(ann.getClass());
    }

}