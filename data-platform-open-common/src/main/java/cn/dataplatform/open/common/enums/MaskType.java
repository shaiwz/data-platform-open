package cn.dataplatform.open.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 敏感类型枚举
 *
 * @author DaoDao
 */
@Getter
@AllArgsConstructor
public enum MaskType {

    /**
     * 无
     */
    GENERAL,

    NAME,

    /**
     * 手机
     */
    PHONE,

    /**
     * 电话
     */
    TEL,

    /**
     * 邮箱
     */
    EMAIL,

    /**
     * 身份证
     */
    ID_CARD,

    /**
     * 银行卡
     */
    BANK_CARD,

    /**
     * 密码
     */
    PASSWORD,
    ;


}