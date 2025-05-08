package cn.dataplatform.open.common.util;


/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2021/2/1
 * @since 1.0.0
 */
public class VersionUtils {

    /**
     * 初始版本号为1.0
     */
    public static final String INIT_VERSION = "1.0";

    /**
     * 获取下一个版本号
     * <p>
     * 支持:1.0升级到1.1
     * 不支持:1.01升级到1.02
     * <p>
     * currentVersion=1.0
     * 如果minorVersion=true,则返回1.1,否则返回2.0
     *
     * @param currentVersion 当前版本
     * @param minorVersion   是否获取小版本
     */
    public static String getNextVersion(String currentVersion, boolean minorVersion) {
        String[] versionSplit = currentVersion.split("\\.");
        if (minorVersion) {
            return versionSplit[0] + "." + (Integer.parseInt(versionSplit[1]) + 1);
        }
        return (Integer.parseInt(versionSplit[0]) + 1) + "." + versionSplit[1];
    }

    /**
     * 使用方法看{@link VersionUtils#getNextVersion(String, boolean)}
     *
     * @param currentVersion 当前版本
     * @return 下一个版本号
     */
    public static String getNextVersion(String currentVersion) {
        return getNextVersion(currentVersion, false);
    }

}
