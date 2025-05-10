package cn.dataplatform.open.common.alarm.robot;


import cn.dataplatform.open.common.alarm.robot.content.Content;
import cn.dataplatform.open.common.alarm.robot.content.LarkContent;
import cn.dataplatform.open.common.alarm.robot.content.TextContent;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.common.exception.LimitException;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/21
 * @since 1.0.0
 */
@Component
public class LarkRobot implements Robot {

    @Value("${dp.alarm.lark.url:https://open.feishu.cn/open-apis/bot/v2/hook/}")
    private String url;

    @Resource
    private RestTemplate restTemplate;

    /**
     * 发送消息到机器人
     *
     * @param content 内容
     */
    @Override
    public void send(String token, Content content) {
        String url = this.url.concat(token);
        Map<String, Object> map = new HashMap<>(1, 2);
        if (content instanceof LarkContent larkContent) {
            map.put("msg_type", "interactive");
            Map<String, Object> cardParam = new HashMap<>(1, 2);
            cardParam.put("type", "template");
            Map<String, Object> dataParam = new HashMap<>();
            dataParam.put("template_id", larkContent.getTemplateId());
            // 设置变量
            dataParam.put("template_variable", larkContent.getTemplateParameter());
            cardParam.put("data", dataParam);
            map.put("card", cardParam);
        } else if (content instanceof TextContent textContent) {
            // text
            map.put("msg_type", "text");
            Map<String, String> contentMap = new HashMap<>(1, 2);
            contentMap.put("text", textContent.getContent());
            map.put("content", contentMap);
        } else {
            throw new ApiException("不支持的消息类型");
        }
        URI uri = URI.create(url);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
        HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(map), headers);
        ResponseEntity<LinkedHashMap<String, Object>> responseEntity = this.restTemplate.exchange(uri,
                HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
                });
        LinkedHashMap<String, Object> entityBody = responseEntity.getBody();
        if (CollUtil.isEmpty(entityBody)) {
            throw new ApiException("执行失败！");
        }
        // 被限流
        if (Objects.equals(entityBody.get("code"), 9499)) {
            throw new LimitException();
        }
        if (!Objects.equals(entityBody.get("code"), 0)) {
            throw new ApiException("执行失败:" + entityBody.get("msg"));
        }
    }

}
