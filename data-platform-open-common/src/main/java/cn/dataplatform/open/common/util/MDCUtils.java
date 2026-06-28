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
package cn.dataplatform.open.common.util;

import cn.dataplatform.open.common.constant.Constant;
import cn.hutool.core.util.StrUtil;
import org.slf4j.MDC;

/**
 * 一句话功能简述
 *
 * @author dingqw
 * @date 2026/3/29
 * @since 1.0.0
 */
public class MDCUtils {

    /**
     * 获取当前线程上下文中的请求ID。
     *
     * @return 当前请求ID，如果未设置则返回 {@code null}
     */
    public static String getRequestId() {
        return MDC.get(Constant.REQUEST_ID);
    }

    /**
     * 设置当前线程上下文中的请求ID。
     *
     * @param requestId 请求ID，建议使用 UUID 或业务生成的唯一字符串
     */
    public static void setRequestId(String requestId) {
        if (StrUtil.isBlank(requestId)) {
            return;
        }
        MDC.put(Constant.REQUEST_ID, requestId);
    }

}
