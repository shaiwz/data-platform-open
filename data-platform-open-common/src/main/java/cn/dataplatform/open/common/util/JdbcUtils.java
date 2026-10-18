package cn.dataplatform.open.common.util;

import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.enums.DataSourceType;
import cn.hutool.core.util.StrUtil;
import com.mysql.cj.jdbc.ConnectionImpl;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/12/22
 * @since 1.0.0
 */
public class JdbcUtils {

    /**
     * MySQL: 提取主机名
     */
    private static final Pattern MYSQL_HOSTNAME_PATTERN = Pattern.compile(
            "jdbc:mysql://([^:/]+)(?::(\\d+))?(?:/.*)?");
    /**
     * PostgreSQL: 提取主机名
     */
    private static final Pattern POSTGRESQL_HOSTNAME_PATTERN = Pattern.compile(
            "jdbc:postgresql://([^:/]+)(?::(\\d+))?(?:/.*)?");

    /**
     * MySQL: 提取端口号
     */
    private static final Pattern MYSQL_PORT_PATTERN = Pattern.compile("jdbc:mysql://.*:(\\d+)(/.*)?");
    /**
     * PostgreSQL: 提取端口号
     */
    private static final Pattern POSTGRESQL_PORT_PATTERN = Pattern.compile("jdbc:postgresql://.*:(\\d+)(/.*)?");

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

    /**
     * 获取数据库配置的端口
     *
     * @param dataSourceType 数据库类型
     * @param url            数据库连接URL
     * @return 端口号
     */
    public static int getPort(DataSourceType dataSourceType, String url) {
        if (StrUtil.isBlank(url)) {
            throw new IllegalArgumentException("JDBC 连接地址不能为空");
        }
        return switch (dataSourceType) {
            case MYSQL, DORIS -> {
                Matcher matcher = MYSQL_PORT_PATTERN.matcher(url);
                yield matcher.find() ? Integer.parseInt(matcher.group(1)) : 3306;
            }
            case POSTGRESQL -> {
                Matcher matcher = POSTGRESQL_PORT_PATTERN.matcher(url);
                yield matcher.find() ? Integer.parseInt(matcher.group(1)) : 5432;
            }
            default -> throw new UnsupportedOperationException("不支持的数据源类型: " + dataSourceType);
        };
    }

    /**
     * 获取hostname
     *
     * @param dataSourceType 数据库类型
     * @param url            数据库连接URL
     * @return hostname
     */
    public static String getHostname(DataSourceType dataSourceType, String url) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        return switch (dataSourceType) {
            case MYSQL, DORIS -> {
                Matcher matcher = MYSQL_HOSTNAME_PATTERN.matcher(url);
                yield matcher.find() ? matcher.group(1) : Constant.LOCALHOST;
            }
            case POSTGRESQL -> {
                Matcher matcher = POSTGRESQL_HOSTNAME_PATTERN.matcher(url);
                yield matcher.find() ? matcher.group(1) : Constant.LOCALHOST;
            }
            default -> throw new UnsupportedOperationException("不支持的数据源类型: " + dataSourceType);
        };
    }

}
