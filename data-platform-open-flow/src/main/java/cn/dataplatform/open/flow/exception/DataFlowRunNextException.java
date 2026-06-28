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
package cn.dataplatform.open.flow.exception;

/**
 * 数据流下游节点运行异常
 *
 * @author dingqianwen
 * @date 2025/6/27
 * @since 1.0.0
 */
public class DataFlowRunNextException extends RuntimeException {

    public DataFlowRunNextException(String message) {
        super(message);
    }

    public DataFlowRunNextException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

}
