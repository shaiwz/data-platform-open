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

@Slf4j
@Configuration
public class OrikaMapper extends ConfigurableMapper implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    public OrikaMapper() {
        super(false);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void configure(MapperFactory factory) {
        Map<String, Mapper> mappers = this.applicationContext.getBeansOfType(Mapper.class);
        for (Mapper<?, ?> mapper : mappers.values()) {
            log.info("Orika register mapper:{}", mapper.getClass().getName());
            factory.registerMapper(mapper);
        }
        Map<String, Converter> converters = applicationContext.getBeansOfType(Converter.class);
        ConverterFactory converterFactory = factory.getConverterFactory();
        for (Converter<?, ?> converter : converters.values()) {
            log.info("Orika register converter:{}", converter.getClass().getName());
            converterFactory.registerConverter(converter);
        }
    }

    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.init();
    }

}
