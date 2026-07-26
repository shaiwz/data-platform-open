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
        // return (Integer.parseInt(versionSplit[0]) + 1) + "." + versionSplit[1];
        // 大版本升级后，小版本号归0
        return (Integer.parseInt(versionSplit[0]) + 1) + ".0";
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


    /**
     * 比较两个版本号的大小
     * <p>
     * 示例:
     * 1.2 > 1.1 -> 返回 1
     * 1.1 < 1.2 -> 返回 -1
     * 1.0 = 1.0.0 -> 返回 0
     *
     * @param version1 版本号1
     * @param version2 版本号2
     * @return 0:相等, 1:version1大于version2, -1:version1小于version2
     */
    public static int compareVersion(String version1, String version2) {
        // 判空处理，根据实际业务需求可调整
        if (version1 == null || version2 == null) {
            throw new IllegalArgumentException("版本不能为空");
        }
        String[] v1Array = version1.split("\\.");
        String[] v2Array = version2.split("\\.");
        int length = Math.max(v1Array.length, v2Array.length);
        for (int i = 0; i < length; i++) {
            // 若长度不一致，缺位视为0，例如 1.0 与 1.0.1 比较，1.0 视为 1.0.0
            int v1 = i < v1Array.length ? Integer.parseInt(v1Array[i]) : 0;
            int v2 = i < v2Array.length ? Integer.parseInt(v2Array[i]) : 0;
            if (v1 > v2) {
                return 1;
            }
            if (v1 < v2) {
                return -1;
            }
        }
        return 0;
    }
}
