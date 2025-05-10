package cn.dataplatform.open.common.alarm.robot;


import cn.dataplatform.open.common.alarm.robot.content.Content;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/21
 * @since 1.0.0
 */
public interface Robot {

    /**
     * 发送消息到机器人
     *
     * @param token   机器人token
     * @param content 内容
     */
    void send(String token, Content content);
}
