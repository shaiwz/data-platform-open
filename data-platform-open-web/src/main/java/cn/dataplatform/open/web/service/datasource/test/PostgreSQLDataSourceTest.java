package cn.dataplatform.open.web.service.datasource.test;

import cn.dataplatform.open.common.enums.DataSourceType;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * PostgreSQL数据源测试
 *
 * @author DaoDao
 */
@Component
public class PostgreSQLDataSourceTest extends JdbcDataSourceTest {

    @Getter
    private final String connectionTestQuery = "SELECT 1";

    /**
     * 获取数据源类型
     *
     * @return 数据源类型
     */
    @Override
    public DataSourceType getDataSourceType() {
        return DataSourceType.POSTGRESQL;
    }

}
