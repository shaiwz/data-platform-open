package cn.dataplatform.open.common.alarm.robot;


import cn.dataplatform.open.common.alarm.robot.content.Content;
import cn.dataplatform.open.common.alarm.robot.content.TextContent;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.common.exception.LimitException;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
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
@Slf4j
public class DingTalkRobot implements Robot {

    @Value("${dp.alarm.ding-talk.url:https://oapi.dingtalk.com/robot/send?access_token=}")
    private String url;

    /**
     * 发送消息到机器人
     *
     * @param content 内容
     */
    public void send(String token, Content content) {
        String url = this.url.concat(token);
        DingTalkClient client = new DefaultDingTalkClient(url);
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("markdown");
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle("<p>");
        String ct = ((TextContent) content).getContent();
        markdown.setText(ct);
        List<String> ats = ReUtil.findAllGroup1("@(.+?)\\s", ct);
        if (CollUtil.isNotEmpty(ats)) {
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            at.setAtMobiles(ats);
            // 包含所有人
            if (ats.contains("all")) {
                at.setIsAtAll(true);
            }
            request.setAt(at);
        }
        request.setMarkdown(markdown);
        try {
            OapiRobotSendResponse response = client.execute(request);
            // 被限流
            if (Objects.equals(response.getErrcode(), 130101L)) {
                throw new LimitException();
            }
            if (!Objects.equals(response.getErrcode(), 0L)) {
                throw new ApiException("执行失败:" + response.getErrmsg());
            }
        } catch (com.taobao.api.ApiException e) {
            log.warn("ApiException", e);
            throw new ApiException("执行失败:" + e.getErrMsg());
        }
    }

}
