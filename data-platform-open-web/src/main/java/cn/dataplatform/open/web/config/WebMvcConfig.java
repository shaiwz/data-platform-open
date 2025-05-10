package cn.dataplatform.open.web.config;


import cn.dataplatform.open.common.introspect.MaskJacksonAnnotationIntrospector;
import cn.dataplatform.open.web.interceptor.AuthInterceptor;
import cn.dataplatform.open.web.interceptor.TokenInterceptor;
import cn.dataplatform.open.web.interceptor.TraceInterceptor;
import cn.dataplatform.open.web.interceptor.WorkspaceInterceptor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;


/**
 * 〈一句话功能简述〉<br>
 * 〈mvc Interceptor〉
 *
 * @author 丁乾文
 * @date 2021/6/17
 * @since 1.0.0
 */
@Component
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private TraceInterceptor traceInterceptor;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private TokenInterceptor tokenInterceptor;
    @Resource
    private AuthInterceptor authInterceptor;
    @Resource
    private WorkspaceInterceptor workspaceInterceptor;
    @Resource
    private ThreadPoolTaskExecutor taskExecutor;

    /**
     * 静态资源不拦截
     */
    private static final List<String> STATIC_RESOURCE = Arrays.asList(
            // druid
            "/druid/**",
            "/favicon.ico/**",
            "/error/**");
    /**
     * 排除拦截
     */
    private static final List<String> EXCLUDE_PATH = Arrays.asList("/login/**", "/logout/**",
            "/actuator/**",
            // 官网使用
            "/official/**"
    );

    /**
     * 不需要验证工作空间的接口
     */
    private static final List<String> EXCLUDE_WORKSPACE_PATH = Arrays.asList("/user/workspace/my/**",
            "/user/getUserInfo/**", "/role/permission/my/**");

    /**
     * @param registry 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.traceInterceptor).addPathPatterns("/**")
                .excludePathPatterns(STATIC_RESOURCE).order(1);
        registry.addInterceptor(this.tokenInterceptor)
                .excludePathPatterns(STATIC_RESOURCE)
                .excludePathPatterns(EXCLUDE_PATH).addPathPatterns("/**").order(10);
        // 校验工作空间
        registry.addInterceptor(this.workspaceInterceptor)
                .excludePathPatterns(STATIC_RESOURCE)
                .excludePathPatterns(EXCLUDE_PATH)
                .excludePathPatterns(EXCLUDE_WORKSPACE_PATH)
                .addPathPatterns("/**").order(11);
        // 校验接口权限 依赖工作空间
        registry.addInterceptor(this.authInterceptor)
                .excludePathPatterns(STATIC_RESOURCE)
                .excludePathPatterns(EXCLUDE_PATH).addPathPatterns("/**").order(12);
    }


    @Override
    public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
        // 解决enum不匹配问题
        this.objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        this.objectMapper.setAnnotationIntrospector(new MaskJacksonAnnotationIntrospector());
    }


    /**
     * 允许跨域请求
     *
     * @return CorsFilter
     */
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        // 允许跨域
        config.setAllowCredentials(true);
        // 允许访问的头信息,*表示全部
        config.addAllowedHeader(CorsConfiguration.ALL);
        // 预检请求的缓存时间（秒）,即在这个时间段里,对于相同的跨域请求不会再预检了
        config.setMaxAge(18000L);
        // 允许提交请求的方法,*表示全部允许
        config.addAllowedMethod(RequestMethod.GET.name());
        config.addAllowedMethod(RequestMethod.POST.name());
        config.addAllowedOriginPattern("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * 配置异步支持
     *
     * @param configurer configurer
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(30000);
        configurer.setTaskExecutor(this.taskExecutor);
    }

}
