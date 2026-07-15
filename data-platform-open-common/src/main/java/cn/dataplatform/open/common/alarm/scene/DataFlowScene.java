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
package cn.dataplatform.open.common.alarm.scene;

/**
 * 数据流相关告警场景标记接口，实现该接口的场景可按数据流编码过滤
 *
 * @author dingqianwen
 * @date 2026/7/3
 * @since 1.0.0
 */
public interface DataFlowScene extends Scene {

    /**
     * 当前告警场景关联的数据流编码
     *
     * @return 数据流编码
     */
    String getFlowCode();

}