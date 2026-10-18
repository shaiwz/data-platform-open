package cn.dataplatform.open.web.service.datasource.test;


import cn.dataplatform.open.common.enums.DataSourceType;
import lombok.Getter;
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
public class MySQLDataSourceTest extends JdbcDataSourceTest {

    @Getter
    private final String connectionTestQuery = "SELECT 1";

    /**
     * 获取数据源类型
     *
     * @return 数据源类型
     */
    @Override
    public DataSourceType getDataSourceType() {
        return DataSourceType.MYSQL;
    }

}