package cn.dataplatform.open.common.util;

import cn.dataplatform.open.common.enums.MaskType;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;


/**
 * @author DaoDao
 */
public class MaskUtil {

    /**
     * 脱敏
     *
     * @param type   脱敏类型
     * @param string 字符串
     * @return 脱敏后的字符串
     */
    public static String mask(MaskType type, String string) {
        if (StrUtil.isEmpty(string)) {
            return string;
        }
        int length = string.length();
        switch (type) {
            case GENERAL:
                if (length < 3) {
                    string = StrUtil.hide(string, 0, string.length());
                } else if (length < 6) {
                    string = StrUtil.hide(string, 1, length - 1);
                } else {
                    string = StrUtil.hide(string, 2, length - 2);
                }
                break;
            case NAME:
                string = DesensitizedUtil.chineseName(string);
                break;
            case ID_CARD:
                string = DesensitizedUtil.idCardNum(string, 2, 2);
                break;
            case PHONE:
                string = DesensitizedUtil.fixedPhone(string);
                break;
            case TEL:
                string = DesensitizedUtil.mobilePhone(string);
                break;
            case EMAIL:
                string = DesensitizedUtil.email(string);
                break;
            case PASSWORD:
                string = DesensitizedUtil.password(string);
                break;
            case BANK_CARD:
                string = DesensitizedUtil.bankCard(string);
                break;
            default:
                return string;
        }
        return string;
    }

}