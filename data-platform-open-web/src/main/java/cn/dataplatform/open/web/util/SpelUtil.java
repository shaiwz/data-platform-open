package cn.dataplatform.open.web.util;

import cn.dataplatform.open.common.exception.ApiException;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/16
 * @since 1.0.0
 */
@Slf4j
public class SpelUtil {

    /**
     * spel表达式解析器
     */
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer DISCOVERER = new StandardReflectionParameterNameDiscoverer();

    /**
     * 解析执行的方法上的spel表达式
     *
     * @param method 方法
     * @param args   参数对象数组
     * @param spel   s
     * @return r
     */
    public static <T> T evaluate(Method method, Object[] args, String spel, Class<T> tClass) {
        //获取方法参数名
        String[] params = DISCOVERER.getParameterNames(method);
        if (params.length == 0) {
            throw new ApiException("没有获取到任何参数");
        }
        EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
        for (int len = 0; len < params.length; len++) {
            context.setVariable(params[len], args[len]);
        }
        try {
            Expression expression = PARSER.parseExpression(spel);
            return expression.getValue(context, tClass);
        } catch (Exception e) {
            log.error("解析spel表达式异常：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析执行的方法上的spel表达式
     *
     * @param arg  参数对象
     * @param spel s
     * @return r
     */
    public static <T> T evaluate(Object arg, String spel, Class<T> tClass) {
        EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
        Map<String, Object> objectMap = BeanUtil.beanToMap(arg);
        if (MapUtil.isNotEmpty(objectMap)) {
            objectMap.forEach(context::setVariable);
        }
        Expression expression = PARSER.parseExpression(spel);
        return expression.getValue(context, tClass);
    }

}
