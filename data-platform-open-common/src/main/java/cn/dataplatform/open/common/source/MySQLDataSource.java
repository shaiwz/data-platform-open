package cn.dataplatform.open.common.source;

import cn.dataplatform.open.common.enums.DataSourceType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * MySQL数据源
 *
 * @author dqw
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class MySQLDataSource extends JdbcSource {

    protected String connectionTestQuery = "SELECT 1";

    /**
     * 数据源类型
     *
     * @return 类型
     */
    @Override
    public DataSourceType type() {
        return DataSourceType.MYSQL;
    }

}
