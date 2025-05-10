package cn.dataplatform.open.common.alarm.robot.content;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HtmlUtil;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/21
 * @since 1.0.0
 */
@Data
public class LarkContent implements Content {

    private String templateId;
    private Map<String, Object> templateParameter;


    /**
     * 转换参数,lark不支持标签,也仅支持spring类型
     *
     * @return new map
     */
    public Map<String, Object> getTemplateParameter() {
        if (CollUtil.isEmpty(templateParameter)) {
            return new HashMap<>();
        }
        Map<String, Object> map = new HashMap<>();
        // 参数转为String,飞书目前传其他类型报错
        this.templateParameter.forEach((k, v) -> {
            switch (v) {
                case Number ignored -> map.put(k, String.valueOf(v));
                case Date date ->
                    // yyyy-MM-dd HH:mm:ss
                        map.put(k, new DateTime(date).toString());
                case LocalDateTime localDateTime -> {
                    // yyyy-MM-dd HH:mm:ss
                    DateTime date = DateUtil.date(localDateTime);
                    map.put(k, date.toString());
                }
                case String string -> {
                    // err_msg: unsupported type of html tag
                    String replace = HtmlUtil.cleanHtmlTag(string);
                    map.put(k, replace);
                }
                case null, default -> map.put(k, v);
            }
        });
        return map;
    }

}
