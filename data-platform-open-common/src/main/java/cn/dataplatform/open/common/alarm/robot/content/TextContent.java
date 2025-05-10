package cn.dataplatform.open.common.alarm.robot.content;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/21
 * @since 1.0.0
 */
@NoArgsConstructor
@Data
public class TextContent implements Content {

    private String content;

    public TextContent(String templateContent) {
        this.content = templateContent;
    }

}
