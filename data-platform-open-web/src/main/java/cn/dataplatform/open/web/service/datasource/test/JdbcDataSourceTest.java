package cn.dataplatform.open.web.service.datasource.test;

import cn.dataplatform.open.common.exception.ApiException;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 一句话功能简述
 *
 * @author dingqw
 * @date 2026/3/29
 * @since 1.0.0
 */
public abstract class JdbcDataSourceTest implements DataSourceTest {

    /**
     * 测试连接
     *
     * @param url      url
     * @param username 用户名
     * @param password 密码
     * @return r
     */
    @SuppressWarnings({"SqlSourceToSinkFlow"})
    @Override
    public boolean testConnection(String url, String username, String password) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            // 执行查询验证连接
            return connection.createStatement().execute(this.getConnectionTestQuery());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), e);
        }
    }

    /**
     * 获取连接测试查询语句
     *
     * @return 查询语句
     */
    public abstract String getConnectionTestQuery();

}
