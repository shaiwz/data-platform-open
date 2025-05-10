package cn.dataplatform.open.web.service.datasource.test;

import cn.dataplatform.open.common.exception.ApiException;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/15
 * @since 1.0.0
 */
public class RabbitMQDataSourceTest implements DataSourceTest {

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
        boolean b;
        ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setUri(url);
            factory.setUsername(username);
            factory.setPassword(password);
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new ApiException(e);
        }
        try (com.rabbitmq.client.Connection connection = factory.newConnection()) {
            int heartbeat = connection.getHeartbeat();
            b = heartbeat > 0;
        } catch (IOException | TimeoutException e) {
            throw new ApiException(e);
        }
        return b;
    }

}
