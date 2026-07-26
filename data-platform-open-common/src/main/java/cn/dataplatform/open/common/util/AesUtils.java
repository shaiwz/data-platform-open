package cn.dataplatform.open.common.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/25
 * @since 1.0.0
 */
@Slf4j
public class AesUtils {

    public static final String TRANSFORMATION = "AES/ECB/PKCS5PADDING";
    public static final String ALGORITHM = "AES";

    /**
     * AES加密数据
     *
     * @param text 明文
     * @return 密文 base64
     */
    public static String encrypt(String text, String secretKey) {
        try {
            if (StrUtil.isEmpty(text)) {
                return text;
            }
            SecretKeySpec sks = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, sks);
            byte[] encrypted = cipher.doFinal(text.getBytes());
            return java.util.Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            log.warn("AES加密失败", ex);
            return text;
        }
    }

    /**
     * 解密
     *
     * @param encrypt 密文
     * @return 明文
     */
    public static String decrypt(String encrypt, String secretKey) {
        try {
            SecretKeySpec sks = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, sks);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypt));
            return new String(original);
        } catch (Exception ex) {
            log.warn("AES解密失败", ex);
            return encrypt;
        }
    }

}