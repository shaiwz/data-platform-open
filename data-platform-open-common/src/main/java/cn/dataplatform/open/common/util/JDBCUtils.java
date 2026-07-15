package cn.dataplatform.open.common.util;

import cn.dataplatform.open.common.enums.DataSourceType;
import com.mysql.cj.jdbc.ConnectionImpl;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.util.Objects;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/12/22
 * @since 1.0.0
 */
public class JDBCUtils {

    /**
     * 应用schema
     *
     * @param type   数据源类型
     * @param conn   连接
     * @param schema schema
     */
    @SneakyThrows
    public static void setSchema(DataSourceType type, Connection conn, String schema) {
        switch (type) {
            case MYSQL, DORIS -> {
                // MySQL切换schema时，如果和当前schema相同则忽略
                if (conn instanceof ConnectionImpl connection) {
                    if (Objects.equals(connection.getDatabase(), schema)) {
                        return;
                    }
                } else {
                    if (Objects.equals(conn.getCatalog(), schema)) {
                        return;
                    }
                }
                // execSQL USE schema
                conn.setCatalog(schema);
            }
            case POSTGRESQL -> {
                // oracle/dm没有进行缓存，每次都需要执行命令
                conn.setSchema(schema);
            }
            case KAFKA, ELASTIC -> {
                // do nothing
            }
            default -> // 兜底方案
                    conn.setSchema(schema);
        }
    }

    /**
     * 获取schema
     *
     * @param type 数据源类型
     * @param conn 连接
     * @return schema
     */
    @SneakyThrows
    public static String getSchema(DataSourceType type, Connection conn) {
        switch (type) {
            case MYSQL, DORIS -> {
                return conn.getCatalog();
            }
            case POSTGRESQL -> {
                return conn.getSchema();
            }
            case KAFKA, ELASTIC -> {
                return null;
            }
            default -> throw new UnsupportedOperationException("不支持的数据源类型: " + type);
        }
    }

}
