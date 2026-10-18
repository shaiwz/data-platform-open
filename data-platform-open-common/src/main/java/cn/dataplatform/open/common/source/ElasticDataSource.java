package cn.dataplatform.open.common.source;

import cn.dataplatform.open.common.enums.DataSourceType;
import cn.dataplatform.open.common.exception.DataSourceException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.Version;
import org.elasticsearch.client.*;

import java.io.IOException;
import java.util.Arrays;

/**
 * ES 数据源
 *
 * @author dqw
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class ElasticDataSource extends Source {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    /**
     * 以 ES 7.15.0 为分界线，低于此版本用 RestHighLevelClient，高于等于此版本用新的 ElasticsearchClient
     */
    public static final Version NEW_CLIENT_VERSION_THRESHOLD = Version.fromString("7.15.0");

    /**
     * 连接超时时间（毫秒）建立 TCP 连接的超时时间
     */
    private int connectTimeout = 30_000;

    /**
     * 读取超时时间（毫秒）两次数据包之间的最大间隔时间
     */
    private int socketTimeout = 60_000;

    /**
     * 连接请求超时时间（毫秒） 从连接池获取连接的超时时间
     */
    private int connectionRequestTimeout = 30_000;
    /**
     * 每个路由的最大连接数
     */
    @Min(value = 1)
    private int maxConnPerRoute = 10;

    /**
     * 最大连接数
     */
    @Min(value = 1)
    private int maxConnTotal = 50;

    /**
     * 客户端，支持以下
     *
     * @see ElasticsearchClient >= NEW_CLIENT_VERSION_THRESHOLD 使用
     * @see RestHighLevelClient &lt; NEW_CLIENT_VERSION_THRESHOLD 使用
     */
    @Setter(AccessLevel.NONE)
    private volatile Object client;

    /**
     * 初始化数据源
     */
    @Override
    public void init() {
        this.connectTimeout = this.getIntProperty(LambdaUtil.getFieldName(this::getConnectTimeout), this.connectTimeout);
        this.socketTimeout = this.getIntProperty(LambdaUtil.getFieldName(this::getSocketTimeout), this.socketTimeout);
        this.connectionRequestTimeout = this.getIntProperty(LambdaUtil.getFieldName(
                this::getConnectionRequestTimeout), this.connectionRequestTimeout);
        this.maxConnPerRoute = this.getIntProperty(LambdaUtil.getFieldName(this::getMaxConnPerRoute),
                this.maxConnPerRoute);
        this.maxConnTotal = this.getIntProperty(LambdaUtil.getFieldName(this::getMaxConnTotal), this.maxConnTotal);
    }

    /**
     * 获取客户端连接
     *
     * @return 客户端连接
     */
    public Object getClient() {
        if (this.client == null) {
            synchronized (this) {
                if (this.client == null) {
                    log.info("初始化 Elastic 数据源: {}", this.url);
                    HttpHost[] httpHosts = Arrays.stream(this.url.split(","))
                            .map(HttpHost::create).toArray(HttpHost[]::new);
                    BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
                    if (StrUtil.isNotBlank(this.username)) {
                        basicCredentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(this.username, this.password));
                    }
                    RestClientBuilder restClientBuilder = RestClient.builder(httpHosts)
                            .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                                    .setConnectTimeout(this.connectTimeout)
                                    .setSocketTimeout(this.socketTimeout)
                                    .setConnectionRequestTimeout(this.connectionRequestTimeout));
                    restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                            .setDefaultCredentialsProvider(basicCredentialsProvider)
                            .setMaxConnPerRoute(this.maxConnPerRoute)
                            .setMaxConnTotal(this.maxConnTotal));
                    RestClient restClient = restClientBuilder.build();
                    try {
                        String versionString = this.getElasticsearchVersion(restClient);
                        log.info("Elasticsearch 服务版本号: {}", versionString);
                        Version version = Version.fromString(versionString);
                        if (version.onOrAfter(NEW_CLIENT_VERSION_THRESHOLD)) {
                            /*
                              ElasticWriteFlowComponent
                              Caused by: com.fasterxml.jackson.databind.exc.InvalidDefinitionException:
                                 Java 8 date/time type `java.time.LocalDateTime` not supported by default: add Module
                                 "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling (through reference chain: java.util.LinkedHashMap["LAST_LOGIN"])
                             */
                            log.info("使用 ElasticsearchClient 作为 Elasticsearch 客户端: {}", this.url);
                            JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(JSON_MAPPER);
                            ElasticsearchTransport transport = new RestClientTransport(restClient, jsonpMapper);
                            this.client = new ElasticsearchClient(transport);
                        } else {
                            log.info("使用 RestHighLevelClient 作为 Elasticsearch 客户端: {}", this.url);
                            IoUtil.close(restClient);
                            this.client = new RestHighLevelClient(restClientBuilder);
                        }
                    } catch (Exception e) {
                        IoUtil.close(restClient);
                        throw new DataSourceException("初始化 Elasticsearch 客户端失败", e);
                    }
                }
            }
        }
        return this.client;
    }

    /**
     * 获取Elasticsearch版本号
     *
     * @param restClient RestClient实例
     * @return 版本号字符串
     * @throws IOException 如果请求失败
     */
    private String getElasticsearchVersion(RestClient restClient) throws IOException {
        Response response = restClient.performRequest(new Request("GET", "/"));
        JsonNode root = JSON_MAPPER.readTree(response.getEntity().getContent());
        return root.path("version").path("number").asText();
    }

    /**
     * 类型
     *
     * @return 类型
     */
    @Override
    public DataSourceType type() {
        return DataSourceType.ELASTIC;
    }

    /**
     * 健康检查，失败抛出异常
     *
     * @throws Exception 检查失败原因
     */
    @Override
    public void health() throws Exception {
        Object client = this.getClient();
        if (client instanceof ElasticsearchClient elasticsearchClient) {
            // 调用 Elasticsearch 的 ping 方法来检查健康状态
            BooleanResponse pingResponse = elasticsearchClient.ping();
            if (!pingResponse.value()) {
                throw new DataSourceException("Elasticsearch ping 失败");
            }
            return;
        }
        if (client instanceof RestHighLevelClient restHighLevelClient) {
            if (!restHighLevelClient.ping(RequestOptions.DEFAULT)) {
                throw new DataSourceException("Elasticsearch ping 失败");
            }
            return;
        }
        throw new DataSourceException("不支持的 Elasticsearch 客户端类型");
    }


    /**
     * 关闭资源
     */
    @Override
    public synchronized void close() {
        log.info("关闭 Elasticsearch数据源: {}({})", this.code, this.name);
        if (this.client != null) {
            try {
                if (this.client instanceof ElasticsearchClient elasticsearchClient) {
                    // 关闭底层的 Transport
                    ElasticsearchTransport elasticsearchTransport = elasticsearchClient._transport();
                    elasticsearchTransport.close();
                } else if (this.client instanceof RestHighLevelClient restHighLevelClient) {
                    restHighLevelClient.close();
                }
                log.info("Elasticsearch 数据源已关闭");
            } catch (Exception e) {
                log.error("关闭 Elasticsearch 客户端时出错", e);
            }
            this.client = null;
        }
    }

}