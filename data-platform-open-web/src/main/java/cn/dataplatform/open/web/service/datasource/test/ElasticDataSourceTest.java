package cn.dataplatform.open.web.service.datasource.test;

import cn.dataplatform.open.common.enums.DataSourceType;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/15
 * @since 1.0.0
 */
@Component
public class ElasticDataSourceTest implements DataSourceTest {

    /**
     * 测试连接
     *
     * @param url      url
     * @param username 用户名
     * @param password 密码
     * @return r
     */
    @SneakyThrows
    @Override
    public boolean testConnection(String url, String username, String password) {
        String[] split = url.split(",");
        HttpHost[] httpHosts = new HttpHost[split.length];
        for (int i = 0; i < split.length; i++) {
            httpHosts[i] = HttpHost.create(split[i]);
        }
        BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
        if (StrUtil.isNotBlank(username)) {
            basicCredentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));
        }
        RestClient restClient = RestClient.builder(httpHosts)
                .setHttpClientConfigCallback(httpAsyncClientBuilder ->
                        httpAsyncClientBuilder.setDefaultCredentialsProvider(basicCredentialsProvider))
                .build();
        try {
            // 直接使用 / 端点测试连接，所有版本都支持
            Response response = restClient.performRequest(new Request("GET", "/"));
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode >= 200 && statusCode < 300;
        } finally {
            IoUtil.close(restClient);
        }
    }

    /**
     * 获取数据源类型
     *
     * @return 数据源类型
     */
    @Override
    public DataSourceType getDataSourceType() {
        return DataSourceType.ELASTIC;
    }

}
