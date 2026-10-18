package cn.dataplatform.open.common.source;

import cn.dataplatform.open.common.enums.DataSourceType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.regex.Pattern;

/**
 * DorisDataSource
 *
 * @author dqw
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class DorisDataSource extends JdbcSource {

    private static final Pattern HOSTNAME = Pattern.compile("jdbc:mysql://([^:/]+)(?::(\\d+))?(?:/.*)?");
    private static final String DEFAULT_HOSTNAME = "localhost";


    private List<String> beNodes;
    private List<String> feNodes;

    /**
     * 数据源类型
     *
     * @return 类型
     */
    @Override
    public DataSourceType type() {
        return DataSourceType.DORIS;
    }

}