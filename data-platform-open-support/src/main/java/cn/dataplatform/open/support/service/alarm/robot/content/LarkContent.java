/*
 * ============================================================================
 *
 *                    数海文舟 (DATA PLATFORM) 版权所有 © 2025
 *
 *       本软件受著作权法和国际版权条约保护，本软件受著作权法和国际版权条约保护，未经明确书面授权，任何单位或个人不得对本软件进行复制、修改、分发、
 *       逆向工程、商业用途等任何形式的非法使用。违者将面临人民币100万元的
 *       法定罚款及可能的法律追责。
 *
 *       举报侵权行为可获得实际罚款金额40%的现金奖励。
 *       举报渠道：
 *           - 法务邮箱：dingqw@shaiwz.com，761945125@qq.com
 *
 *       COPYRIGHT (C) 2025 dingqianwen COMPANY. ALL RIGHTS RESERVED.
 *
 * ============================================================================
 */
package cn.dataplatform.open.support.service.alarm.robot.content;

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
        if (CollUtil.isEmpty(this.templateParameter)) {
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
