package cn.dataplatform.open.common.util;

import cn.dataplatform.open.common.exception.ApiException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Iterator;
import java.util.Set;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/4/4
 * @since 1.0.0
 */
public class ValidationUtils {

    /**
     * 创建校验器
     */
    private final static Validator VALIDATOR;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            VALIDATOR = factory.getValidator();
        }
    }

    /**
     * 校验对象中注解 @NotBlank @NotNull 等
     *
     * @param instance 被校验的类
     */
    public static void validate(Object instance) {
        if (instance == null) {
            throw new ApiException("校验对象不能为 null");
        }
        // 验证某个对象,，其实也可以只验证其中的某一个属性的
        Set<ConstraintViolation<Object>> constraintViolations = VALIDATOR.validate(instance);
        Iterator<ConstraintViolation<Object>> iter = constraintViolations.iterator();
        if (iter.hasNext()) {
            ConstraintViolation<Object> next = iter.next();
            String messageTemplate = next.getMessageTemplate();
            if (messageTemplate.startsWith("{") && messageTemplate.endsWith("}")) {
                throw new ApiException("[" + next.getPropertyPath().toString() + "]" + next.getMessage());
            } else {
                throw new ApiException(next.getMessage());
            }
        }
    }

}
