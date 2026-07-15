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

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/12/17
 * @since 1.0.0
 */
public class IdUtils extends IdUtil {

    /**
     * 生成简单ID，格式：时间戳(秒级别的十六进制)+6位随机字符串
     *
     * @return 简单ID
     */
    public static String getSimpleId() {
        return Integer.toHexString((int) DateUtil.currentSeconds()) + RandomUtil.randomString(7);
    }

}
