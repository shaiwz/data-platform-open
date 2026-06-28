package cn.dataplatform.open.support.service.alarm.impl;

import cn.dataplatform.open.common.alarm.scene.Scene;
import cn.dataplatform.open.common.body.AlarmMessageBody;
import cn.dataplatform.open.common.body.AlarmSceneMessageBody;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.util.ParallelStreamUtils;
import cn.dataplatform.open.support.service.alarm.AlarmSceneService;
import cn.dataplatform.open.support.service.alarm.AlarmService;
import cn.dataplatform.open.support.store.entity.AlarmScene;
import cn.dataplatform.open.support.store.mapper.AlarmSceneMapper;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/2/22
 * @since 1.0.0
 */
@Slf4j
@Service
public class AlarmSceneServiceImpl extends ServiceImpl<AlarmSceneMapper, AlarmScene>
        implements AlarmSceneService {

    @Resource
    private AlarmService alarmService;

    /**
     * 监听告警场景消息处理
     *
     * @param alarmSceneMessageBody 告警场景消息
     */
    @Override
    public void alarm(AlarmSceneMessageBody alarmSceneMessageBody) {
        // 编码可以为空,如果没有传,则不需要此条件,所有工作空间此场景的告警都发送一次
        // 例如服务上下线,就没办法获取到工作空间编码
        String workspaceCode = alarmSceneMessageBody.getWorkspaceCode();
        Scene scene = alarmSceneMessageBody.getScene();
        String serverName = alarmSceneMessageBody.getServerName();
        // 如果告警场景配置多个，每个也要发送一次
        List<AlarmScene> alarmScenes = this.lambdaQuery()
                .eq(workspaceCode != null, AlarmScene::getWorkspaceCode, workspaceCode)
                .eq(AlarmScene::getServerName, serverName)
                .eq(AlarmScene::getScene, scene.scene())
                .eq(AlarmScene::getStatus, Status.ENABLE.name())
                .list();
        if (CollUtil.isEmpty(alarmScenes)) {
            log.info("场景配置不存在");
            return;
        }
        // 修复mdc传递问题
        ParallelStreamUtils.forEach(alarmScenes, alarmScene -> {
            // 发送告警消息
            AlarmMessageBody alarmMessageBody = new AlarmMessageBody();
            alarmMessageBody.setParameter(BeanUtil.beanToMap(scene));
            alarmMessageBody.setServerName(alarmSceneMessageBody.getServerName());
            alarmMessageBody.setInstanceId(alarmSceneMessageBody.getInstanceId());
            alarmMessageBody.setAlarmTime(alarmSceneMessageBody.getAlarmTime());
            alarmMessageBody.setWorkspaceCode(alarmScene.getWorkspaceCode());
            // 机器人编码以及模板编码
            alarmMessageBody.setRobotCode(alarmScene.getRobotCode());
            alarmMessageBody.setTemplateCode(alarmScene.getTemplateCode());
            this.alarmService.alarm(alarmMessageBody, alarmScene);
        }, false);
    }

}
