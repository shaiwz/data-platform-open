package cn.dataplatform.open.web.service.datasource.test;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/15
 * @since 1.0.0
 */
public class ElasticDataSourceTest implements DataSourceTest {

    /**
     * 测试连接
     *
     * @param url      url
     * @param username 用户名
     * @param password 密码
     * @return r
     */
    @Override
    public boolean testConnection(String url, String username, String password) {
        String[] split = url.split(",");
        HttpHost[] httpHosts = new HttpHost[split.length];
        for (int i = 0; i < split.length; i++) {
            httpHosts[i] = HttpHost.create(split[i]);
        }
        BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
        if (StrUtil.isNotBlank(username)) {
            basicCredentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        }
        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);
        restClientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder ->
                httpAsyncClientBuilder.setDefaultCredentialsProvider(basicCredentialsProvider));
        RestClient restClient = restClientBuilder.build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient elasticsearchClient = new ElasticsearchClient(transport);
        try {
            BooleanResponse pingResponse = elasticsearchClient.ping();
            return pingResponse.value();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IoUtil.close(restClient);
            elasticsearchClient.shutdown();
        }
    }

}
