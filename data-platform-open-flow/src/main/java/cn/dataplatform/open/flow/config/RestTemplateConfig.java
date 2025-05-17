package cn.dataplatform.open.flow.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author dingqianwen
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setHttpClient(httpClient);
        httpRequestFactory.setConnectionRequestTimeout(10000);
        httpRequestFactory.setConnectTimeout(10000);
        httpRequestFactory.setReadTimeout(10000);
        return new RestTemplate(httpRequestFactory);
    }

    /**
     * 定义httpClient线程池管理器
     *
     * @return poolingConnectionManager
     */
    @Bean
    public HttpClientConnectionManager poolingConnectionManager() {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        // 设置最大连接数
        poolingHttpClientConnectionManager.setMaxTotal(200);
        // 设置每个路由基础的默认连接数
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(100);
        // 设置检查永久链接可用性的间隔时间
        // poolingHttpClientConnectionManager.setValidateAfterInactivity(20000);
        poolingHttpClientConnectionManager.setDefaultConnectionConfig(ConnectionConfig.custom().setValidateAfterInactivity(TimeValue.of(20000, TimeUnit.SECONDS)).build());
        return poolingHttpClientConnectionManager;
    }

    /**
     * 定义httpClient建造器
     *
     * @return r
     */
    @Bean
    public HttpClient httpClient(HttpClientConnectionManager poolingConnectionManager) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(poolingConnectionManager);
        return httpClientBuilder.build();
    }

}

