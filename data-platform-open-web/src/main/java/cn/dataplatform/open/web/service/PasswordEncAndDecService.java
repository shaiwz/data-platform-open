package cn.dataplatform.open.web.service;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/2
 * @since 1.0.0
 */
public interface PasswordEncAndDecService {

    /**
     * 加密
     *
     * @param password 密码
     * @return 加密后的密码
     */
    String encrypt(String password);

    /**
     * 解密
     *
     * @param password 密码
     * @return 解密后的密码
     */
    String decrypt(String password);

}
