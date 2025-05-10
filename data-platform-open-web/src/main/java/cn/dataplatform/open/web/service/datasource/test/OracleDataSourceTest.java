package cn.dataplatform.open.web.service.datasource.test;

import cn.dataplatform.open.common.exception.ApiException;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/15
 * @since 1.0.0
 */
@Slf4j
public class OracleDataSourceTest implements DataSourceTest {

    /**
     * 获取数据库连接
     *
     * @param url      数据库连接地址
     * @param username 用户名
     * @param password 密码
     * @return 数据库连接
     */
    @Override
    public boolean testConnection(String url, String username, String password) {
        // 尝试连接数据库
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            log.debug("数据库连接成功: {}", url);
            // 可以进一步执行简单的查询来验证连接
            if (connection != null) {
                log.debug("数据库版本: {}", connection.getMetaData().getDatabaseProductVersion());
            }
            return true;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new ApiException(e.getMessage());
        }
    }

}
