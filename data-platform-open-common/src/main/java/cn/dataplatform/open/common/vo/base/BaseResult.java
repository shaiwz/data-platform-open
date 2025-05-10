package cn.dataplatform.open.common.vo.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 丁乾文
 * @create 2019/9/30
 * @since 1.0.0
 */
@Data
public class BaseResult implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 返回状态
     */
    private ResultState state;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回码
     */
    private Integer code;

    /**
     * 时间轴
     */
    @JsonFormat(timezone = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;

    public BaseResult() {
        this.state = ResultState.SUCCESS;
        this.message = "执行成功";
        this.code = 200;
        this.timestamp = new Date();
    }

    public static BaseResult ok() {
        return new BaseResult();
    }

    public static BaseResult err() {
        return err(400, "执行失败");
    }

    public static BaseResult err(Integer code, String message) {
        BaseResult baseResult = new BaseResult();
        baseResult.setState(ResultState.ERROR);
        baseResult.setMessage(message);
        baseResult.setCode(code);
        baseResult.setTimestamp(new Date());
        return baseResult;
    }

}
