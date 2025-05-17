package cn.dataplatform.open.common.source;

import cn.dataplatform.open.common.enums.DataSourceType;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

/**
 * es数据源
 *
 * @author dqw
 * @since 1.0.0
 */
@Slf4j
@Data
public class ElasticDataSource implements Source {

    private String name;
    private String code;

    /**
     * 连接信息 多个,隔开
     */
    @NotBlank
    private String url;
    @NotBlank
    private String username;
    @NotBlank
    private String password;

    private Boolean isEnableHealth;

    @Setter(AccessLevel.NONE)
    private volatile Object client;

    /**
     * 获取客户端连接
     *
     * @return 客户端连接
     */
    public Object getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
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
                    client = new ElasticsearchClient(transport);
                    // 如果是低于7.7版本的es,使用下面的方式
                    // client = new RestHighLevelClient(RestClient.builder(httpHosts));
                }
            }
        }
        return client;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public DataSourceType type() {
        return DataSourceType.ELASTIC;
    }

    @Override
    public Boolean isEnableHealth() {
        return isEnableHealth;
    }

    /**
     * 健康检查
     *
     * @return true健康
     */
    @Override
    public Boolean health() throws Exception {
        Object client = this.getClient();
        if (client instanceof ElasticsearchClient elasticsearchClient) {
            // 调用 Elasticsearch 的 ping 方法来检查健康状态
            BooleanResponse pingResponse = elasticsearchClient.ping();
            return pingResponse.value();
        }
        return false;
    }


    /**
     * 关闭资源
     */
    @Override
    public void close() {
        if (client != null) {
            if (client instanceof ElasticsearchClient) {
                ((ElasticsearchClient) client).shutdown();
            }
            client = null;
        }
    }

}