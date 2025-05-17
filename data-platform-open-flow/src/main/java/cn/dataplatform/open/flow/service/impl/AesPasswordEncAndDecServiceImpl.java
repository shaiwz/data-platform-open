package cn.dataplatform.open.flow.service.impl;

import cn.dataplatform.open.common.util.Aes128Util;
import cn.dataplatform.open.flow.service.PasswordEncAndDecService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/2
 * @since 1.0.0
 */
@Service
public class AesPasswordEncAndDecServiceImpl implements PasswordEncAndDecService {

    /**
     * 密钥
     */
    @Value("${dp.password.secret-key:Dpdqwa1eDC13%09=}")
    private String secretKey;

    /**
     * 加密
     *
     * @param password 密码
     * @return 加密后的密码
     */
    @Override
    public String encrypt(String password) {
        if (StrUtil.isBlank(password)) {
            return password;
        }
        return Aes128Util.encrypt(password, secretKey);
    }

    /**
     * 解密
     *
     * @param password 密码
     * @return 解密后的密码
     */
    @Override
    public String decrypt(String password) {
        if (StrUtil.isBlank(password)) {
            return password;
        }
        return Aes128Util.decrypt(password, secretKey);
    }

}
