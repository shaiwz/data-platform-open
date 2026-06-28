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
package cn.dataplatform.open.support.service.alarm.robot;

import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.common.exception.LimitException;
import cn.dataplatform.open.support.service.alarm.robot.content.Content;
import cn.dataplatform.open.support.service.alarm.robot.content.TextContent;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
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
     * <p>
     * 提及人格式为： @手机号1 @手机号2 @all  后面需要有个空格
     *
     * @param content 内容
     */
    @Override
    public void send(String token, Content content) {
        String url = this.url.concat(token);
        DingTalkClient client = new DefaultDingTalkClient(url);
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("markdown");
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle("<p>");
        String ct = ((TextContent) content).getContent();
        markdown.setText(ct);
        List<String> ats = ReUtil.findAllGroup1("@([^\\s,@]+)", ct);
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
                throw new ApiException("发送机器人消息失败: " + response.getErrmsg());
            }
        } catch (com.taobao.api.ApiException e) {
            throw new ApiException("发送机器人消息失败: " + e.getErrMsg(), e);
        }
    }

}
