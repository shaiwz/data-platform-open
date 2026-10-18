package cn.dataplatform.open.common.source;

import cn.dataplatform.open.common.enums.DataSourceType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Postgre数据源
 *
 * @author DaoDao
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class PostgreSQLDataSource extends JdbcSource {

    protected String connectionTestQuery = "SELECT 1";

    /**
     * 数据源类型
     *
     * @return 类型
     */
    @Override
    public DataSourceType type() {
        return DataSourceType.POSTGRESQL;
    }

}