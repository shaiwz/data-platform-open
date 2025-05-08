package cn.dataplatform.open.common.util;

import jakarta.servlet.http.HttpServletRequest;
import jodd.util.StringPool;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 丁乾文
 * @date 2019/8/14
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IPUtils {

    /**
     * 服务端的ip地址
     */
    public static final String SERVER_IP = IPUtils.getServerIp();

    /**
     * 获取请求IP地址
     *
     * @return 请求的ip地址
     */
    public static String getRequestIp() {
        HttpServletRequest request = HttpServletUtils.getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * 获取服务端ip
     *
     * @return 服务端ip地址
     */
    private static String getServerIp() {
        String clientIp = StringPool.EMPTY;
        //根据网卡取本机配置的IP,定义网络接口枚举类
        Enumeration<NetworkInterface> allNetInterfaces;
        try {
            //获得网络接口
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            //声明一个InetAddress类型ip地址
            InetAddress ip;
            //遍历所有的网络接口
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                //同样再定义网络地址枚举类
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    //InetAddress类包括Inet4Address和Inet6Address
                    if ((ip instanceof Inet4Address)) {
                        if (!"127.0.0.1".equals(ip.getHostAddress())) {
                            clientIp = ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            log.error("SocketException", e);
        }
        return clientIp;
    }

}
