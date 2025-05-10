package cn.dataplatform.open.web.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/9
 * @since 1.0.0
 */
@Data
@Component
@ConditionalOnProperty(prefix = "aliyun.oss", name = "enable", havingValue = "true")
@ConfigurationProperties("aliyun.oss")
public class AliOSSProperties {

    /**
     * 是否启用oss
     */
    private boolean enable;
    private String endPoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String defaultFolder;

}
