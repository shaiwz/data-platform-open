package cn.dataplatform.open.web.exception;



import cn.dataplatform.open.common.alarm.scene.GlobalExceptionHandlingScene;
import cn.dataplatform.open.common.body.AlarmSceneMessageBody;
import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.enums.ErrorCode;
import cn.dataplatform.open.common.event.AlarmSceneEvent;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.common.vo.base.BaseResult;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.vo.workspace.WorkspaceData;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 〈一句话功能简述〉<br>
 * 〈统一异常处理〉
 *
 * @author 丁乾文
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Exception级别未捕获异常发送邮箱警告
     *
     * @param e e
     * @return BaseResult
     */
    @ExceptionHandler(value = Exception.class)
    public Object exception(Exception e) {
        BaseResult result = BaseResult.err();
        log.error("Exception", e);
        // 抛出的未知异常 加上RequestId
        result.setMessage(ErrorCode.DP_500.getMsg().concat(StringPool.AT) + MDC.get(Constant.REQUEST_ID));
        result.setCode(ErrorCode.DP_500.getCode());
        // 告警消息
        AlarmSceneMessageBody alarmMessageBody = new AlarmSceneMessageBody(new GlobalExceptionHandlingScene(e));
        alarmMessageBody.setWorkspaceCode(Optional.ofNullable(Context.getWorkspace()).map(WorkspaceData::getCode).orElse(null));
        this.applicationEventPublisher.publishEvent(new AlarmSceneEvent(alarmMessageBody));
        return result;
    }

    /**
     * 资源未找到异常
     *
     * @return BaseResult
     */
    @ExceptionHandler(value = {NoHandlerFoundException.class})
    public BaseResult noHandlerFoundException() {
        BaseResult result = BaseResult.err();
        result.setMessage(ErrorCode.DP_9999404.getMsg());
        result.setCode(ErrorCode.DP_9999404.getCode());
        return result;
    }

    /**
     * 请求方法不匹配
     *
     * @return BaseResult
     */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public BaseResult httpRequestMethodNotSupportedException() {
        BaseResult result = BaseResult.err();
        result.setMessage(ErrorCode.DP_9999405.getMsg());
        result.setCode(ErrorCode.DP_9999405.getCode());
        return result;
    }

    /**
     * 验证异常
     * 需要把异常信息显示到页面的情况
     *
     * @param e e
     * @return BaseResult
     */
    @ExceptionHandler(value = ValidationException.class)
    public BaseResult validationException(ValidationException e) {
        log.warn("ValidationException:" + e.getMessage());
        BaseResult result = BaseResult.err();
        result.setMessage(e.getMessage());
        result.setCode(ErrorCode.DP_99990100.getCode());
        return result;
    }

    /**
     * apiException
     * 说明:{@link ApiException#ApiException(String, Object...)}
     *
     * @param e e
     * @return BaseResult
     */
    @ExceptionHandler(value = ApiException.class)
    public BaseResult apiException(ApiException e) {
        log.warn("ApiException", e);
        BaseResult result = BaseResult.err();
        result.setMessage(e.getMessage());
        result.setCode(e.getCode());
        return result;
    }

    /**
     * 非法或不适当的参数
     *
     * @return BaseResult
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public BaseResult illegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException", e);
        BaseResult result = BaseResult.err();
        result.setMessage(ErrorCode.DP_99990100.getMsg());
        result.setCode(ErrorCode.DP_99990100.getCode());
        return result;
    }

    /**
     * bindException
     *
     * @return BaseResult
     */
    @ExceptionHandler(value = BindException.class)
    public BaseResult bindException(BindException e) {
        log.warn("BindException", e);
        BaseResult result = BaseResult.err();
        FieldError error = e.getFieldError();
        result.setMessage(Optional.of(error).map(FieldError::getDefaultMessage).orElse(ErrorCode.DP_99990100.getMsg()));
        result.setCode(ErrorCode.DP_99990100.getCode());
        return result;
    }

    /**
     * 不支持的内容类型
     * 例如:Content type 'application/x-www-form-urlencoded;charset=UTF-8' not supported
     *
     * @return BaseResult
     */
    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public BaseResult httpMediaTypeNotSupportedException() {
        BaseResult result = BaseResult.err();
        result.setMessage(ErrorCode.DP_99990001.getMsg());
        result.setCode(ErrorCode.DP_99990001.getCode());
        return result;
    }

    /**
     * 缺少所需的请求正文
     *
     * @return BaseResult
     */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public BaseResult httpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException", e);
        BaseResult result = BaseResult.err();
        result.setMessage(ErrorCode.DP_10010003.getMsg());
        result.setCode(ErrorCode.DP_10010003.getCode());
        return result;
    }

    /**
     * 参数异常
     * 配合@Validated使用
     *
     * @return BaseResult
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public BaseResult constraintViolationException(ConstraintViolationException e) {
        log.warn("ConstraintViolationException", e);
        BaseResult result = BaseResult.err();
        List<ConstraintViolation<?>> arrayList = new ArrayList<>(e.getConstraintViolations());
        ConstraintViolation<?> constraintViolation = arrayList.getFirst();
        result.setMessage(constraintViolation.getMessage());
        result.setCode(ErrorCode.DP_99990100.getCode());
        return result;
    }

    /**
     * 方法参数无效
     * {javax.validation.constraints.NotNull.message}
     *
     * @return BaseResult
     */
    @SneakyThrows
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Object methodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException:" + e.getMessage());
        BaseResult result = BaseResult.err();
        Field source = ObjectError.class.getDeclaredField("source");
        if (!Modifier.isPublic(source.getModifiers())) {
            source.setAccessible(true);
        }
        ConstraintViolation<?> constraintViolation = (ConstraintViolation<?>) source.get(e.getBindingResult().getFieldError());
        String messageTemplate = constraintViolation.getMessageTemplate();
        // 如果使用默认的{javax.validation.constraints.***.message}
        if (messageTemplate.startsWith(StrUtil.DELIM_START) && messageTemplate.endsWith(StrUtil.DELIM_END)) {
            result.setMessage(constraintViolation.getPropertyPath().toString() + " " + constraintViolation.getMessage());
        } else {
            result.setMessage(constraintViolation.getMessage());
        }
        result.setCode(ErrorCode.DP_99990002.getCode());
        return result;
    }

    /**
     * 重复提交异常
     *
     * @param e e
     * @return BaseResult
     */
    @ExceptionHandler(value = ReSubmitException.class)
    public BaseResult reSubmitException(ReSubmitException e) {
        BaseResult result = BaseResult.err();
        result.setMessage(e.getMessage());
        result.setCode(e.getCode());
        return result;
    }


    /**
     * No static resource dataflow/pageDesignHistory
     *
     * @param e e
     * @return r
     */
    @ExceptionHandler(value = NoResourceFoundException.class)
    public BaseResult noResourceFoundException(NoResourceFoundException e) {
        BaseResult result = BaseResult.err();
        result.setMessage("资源不存在:" + e.getResourcePath());
        result.setCode(ErrorCode.DP_9999404.getCode());
        return result;
    }

    /**
     * 数据库连接失败
     *
     * @param e e
     * @return r
     */
    @ExceptionHandler(value = CommunicationsException.class)
    public BaseResult communicationsException(CommunicationsException e) {
        log.warn("CommunicationsException", e);
        BaseResult result = BaseResult.err();
        result.setMessage(ErrorCode.DP_10011042.getMsg());
        result.setCode(ErrorCode.DP_10011042.getCode());
        return result;
    }

    /**
     * 不支持的操作异常
     *
     * @param e e
     * @return r
     */
    @ExceptionHandler(value = UnsupportedOperationException.class)
    public BaseResult unsupportedOperationException(UnsupportedOperationException e) {
        log.warn("UnsupportedOperationException", e);
        BaseResult result = BaseResult.err();
        result.setCode(ErrorCode.DP_10011043.getCode());
        result.setMessage(Optional.ofNullable(e.getMessage()).orElse(ErrorCode.DP_10011043.getMsg()));
        return result;
    }

}
