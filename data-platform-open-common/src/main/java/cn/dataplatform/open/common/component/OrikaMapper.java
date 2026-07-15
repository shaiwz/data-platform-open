package cn.dataplatform.open.common.component;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2026/1/5
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class OrikaMapper extends ConfigurableMapper implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public OrikaMapper() {
        super(false);
    }

    /**
     * 配置MapperFactory
     *
     * @param factory MapperFactory
     */
    @Override
    protected void configure(MapperFactory factory) {
        @SuppressWarnings("rawtypes")
        Map<String, Mapper> mappers = this.applicationContext.getBeansOfType(Mapper.class);
        for (Mapper<?, ?> mapper : mappers.values()) {
            factory.registerMapper(mapper);
        }
        @SuppressWarnings("rawtypes")
        Map<String, Converter> converters = this.applicationContext.getBeansOfType(Converter.class);
        ConverterFactory converterFactory = factory.getConverterFactory();
        for (Converter<?, ?> converter : converters.values()) {
            converterFactory.registerConverter(converter);
        }
    }

    /**
     * 设置ApplicationContext
     *
     * @param applicationContext a
     */
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.init();
    }

}
