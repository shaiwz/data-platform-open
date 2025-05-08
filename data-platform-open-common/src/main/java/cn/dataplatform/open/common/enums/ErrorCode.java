package cn.dataplatform.open.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈通用错误码〉
 *
 * @author 丁乾文
 * @since 1.0.0
 */
@Getter
public enum ErrorCode {

    /**
     * error code and msg
     */
    DP_99990099(99990099, "校验错误"),
    DP_99990100(9999100, "参数异常"),
    DP_99990101(9999101, "登录异常"),
    DP_4009(4009, "用户未登录"),
    DP_4010(4010, "没有可用工作空间,请联系管理员！"),
    DP_4011(4011, "你没有此工作空间权限,已为你设置默认工作空间,请重新登录！"),
    DP_4012(4012, "当前没有设置默认工作空间,请重新登录！"),

    DP_99990401(99990401, "无访问权限"),
    DP_99990402(99990402, "验证信息已失效"),
    DP_500(500, "未知异常"),
    DP_99990501(501, "服务端异常"),
    DP_99990403(9999403, "无权访问"),
    DP_9999404(9999404, "找不到指定资源"),
    DP_9999405(9999405, "请求方法不匹配"),
    DP_99990001(99990001, "不支持的内容类型"),
    DP_99990002(99990002, "方法参数无效"),
    /**
     * 请求头缺失
     */
    DP_99990003(99990003, "请求头缺失"),
    DP_10010002(10010002, "TOKEN解析失败"),
    DP_10010004(10010004, "TOKEN为空"),
    DP_10010003(10010003, "缺少所需的请求正文"),
    DP_10011032(10011032, "不存在此邮箱"),
    DP_10011033(10011033, "邮箱格式错误"),
    DP_10011034(10011034, "邮箱发送出错"),
    DP_10011035(10011035, "异常警告"),
    DP_10011036(10011036, "OSS上传文件异常"),
    DP_10011038(10011038, "请勿重复操作"),
    DP_10011039(10011039, "验证Token失败"),
    /**
     * 工作空间不存在
     */
    DP_10011040(10021040, "工作空间不存在"),
    /**
     * 工作空间未指定
     */
    DP_10011041(10021041, "工作空间未指定"),
    /**
     * Caused by: com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
     * <p>
     * The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.
     */
    DP_10011042(10021042, "数据库连接失败"),
    /**
     * DP_99990004 接口被限流
     */
    DP_99990004(99990004, "接口被限流"),

    /**
     * 不支持的操作
     */
    DP_10011043(10011043, "不支持的操作"),
    /**
     * mybatis ExecutorException
     */
    DP_10011044(10011044, "执行器异常"),

    ;

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    static final Map<Integer, ErrorCode> map = new HashMap<>();

    static {
        ErrorCode[] values = values();
        for (ErrorCode value : values) {
            map.put(value.getCode(), value);
        }
    }

    public static String getMagByCode(Integer code) {
        return map.get(code).getMsg();
    }

    public static ErrorCode get(Integer code) {
        return map.get(code);
    }

}
