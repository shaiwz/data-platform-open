package cn.dataplatform.open.web.service.datasource.test;


import cn.dataplatform.open.common.exception.ApiException;

import java.sql.*;

/**
 * @author DaoDao
 */
public class PostgreSQLDataSourceTest implements DataSourceTest {

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
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select 1");
            return resultSet.next();
        } catch (SQLException e) {
            throw new ApiException(e);
        }
    }

}
