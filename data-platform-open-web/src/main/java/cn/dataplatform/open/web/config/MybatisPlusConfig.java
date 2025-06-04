package cn.dataplatform.open.web.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 〈MybatisPlusConfig〉
 *
 * @author 丁乾文
 * @date 2021/6/23 8:13 下午
 * @since 1.0.0
 */
@Component
public class MybatisPlusConfig {

    /**
     * mybatis plus 拦截器
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setOverflow(true);
        paginationInnerInterceptor.setMaxLimit(5000L);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }

}
