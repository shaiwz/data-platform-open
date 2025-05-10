package cn.dataplatform.open.common.alarm.robot;


import cn.dataplatform.open.common.alarm.robot.content.Content;
import cn.dataplatform.open.common.alarm.robot.content.TextContent;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.common.exception.LimitException;
import cn.hutool.core.collection.CollUtil;
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
public class WeComRobot implements Robot {

    @Value("${dp.alarm.we-com.url:https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=}")
    private String url;

    @Resource
    private RestTemplate restTemplate;

    /**
     * 发送消息到机器人
     *
     * @param content 内容
     */
    public void send(String token, Content content) {
        Map<String, Object> map = new HashMap<>(10);
        map.put("msgtype", "markdown");
        Map<String, String> contentMap = new HashMap<>(2);
        contentMap.put("content", ((TextContent) content).getContent());
        map.put("markdown", contentMap);
        URI uri = URI.create(this.url.concat(token));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<LinkedHashMap<String, String>> responseEntity = this.restTemplate.exchange(uri,
                HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
                });
        LinkedHashMap<String, String> entityBody = responseEntity.getBody();
        if (CollUtil.isEmpty(entityBody)) {
            throw new ApiException("执行失败！");
        }
        // 被限流
        if (Objects.equals(entityBody.get("errcode"), "45009")) {
            throw new LimitException();
        }
        if (!Objects.equals(entityBody.get("errcode"), "0")) {
            throw new ApiException("执行失败:" + entityBody.get("errmsg"));
        }
    }


}
