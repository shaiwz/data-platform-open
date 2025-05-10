package cn.dataplatform.open.web.service.datasource.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/15
 * @since 1.0.0
 */
@Slf4j
public class RocketMQDataSourceTest implements DataSourceTest {

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
        // 创建一个默认的生产者实例
        DefaultMQProducer producer = new DefaultMQProducer("TestConnectionGroup");
        producer.setNamesrvAddr(url);
        try {
            // 启动生产者,尝试与 NameServer 建立连接
            producer.start();
            return true; // 如果启动成功,说明连接正常
        } catch (MQClientException e) {
            // 捕获异常,连接失败
            log.error("RocketMQ连接失败", e);
            return false;
        } finally {
            // 关闭生产者
            producer.shutdown();
        }
    }

}
