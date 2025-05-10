package cn.dataplatform.open.web.service.datasource.test;

import cn.dataplatform.open.common.exception.ApiException;
import cn.hutool.core.util.StrUtil;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/15
 * @since 1.0.0
 */
public class KafkaDataSourceTest implements DataSourceTest {

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
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, url);
        // 设置 SASL 认证相关配置
        if (StrUtil.isNotBlank(username) && StrUtil.isNotBlank(password)) {
            props.put("security.protocol", "SASL_SSL");
            props.put("sasl.mechanism", "PLAIN");
            props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                    "username=\"" + username + "\" " +
                    "password=\"" + password + "\";");
        }
        try (AdminClient adminClient = AdminClient.create(props)) {
            // 设置描述集群的选项
            DescribeClusterOptions options = new DescribeClusterOptions().timeoutMs(5000);
            DescribeClusterResult result = adminClient.describeCluster(options);
            String clusterId = result.clusterId().get(5, TimeUnit.SECONDS);
            return clusterId != null;
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

}
