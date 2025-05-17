package cn.dataplatform.open.common.aspect;

import cn.dataplatform.open.common.util.HttpServletUtils;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Stream;

/**
 * 〈请求日志〉
 *
 * @author 丁乾文
 * @date 2019/8/13
 * @since 1.0.0
 */
@Order(-20)
@Component
@Aspect
@Slf4j
public class RequestLogAspect {

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 打印请求日志
     *
     * @param joinPoint 连接点
     * @return 被代理类方法执行结果
     * @throws Throwable .
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        StringBuilder sb = new StringBuilder("\n");
        long start = System.currentTimeMillis();
        try {
            sb.append("┏━━━━━━━━请求日志━━━━━━━━\n");
            sb.append("┣ 链接: ").append(HttpServletUtils.getRequest().getRequestURL()).append("\n");
            Object[] objects = this.argsExcludeClass(joinPoint.getArgs());
            sb.append("┣ 参数: ").append(this.objectMapper.writeValueAsString(objects)).append("\n");
            Object proceed = joinPoint.proceed();
            String valueAsString = this.objectMapper.writeValueAsString(proceed);
            sb.append("┣ 结果: ").append(StrUtil.maxLength(valueAsString, 5000)).append("\n");
            return proceed;
        } catch (Throwable throwable) {
            sb.append("┣ 异常: ").append(throwable).append("\n");
            throw throwable;
        } finally {
            sb.append("┣ 时间: ").append(System.currentTimeMillis() - start).append("ms\n");
            sb.append("┗━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("{}", sb);
        }
    }

    /**
     * 参数过滤调一部分类
     *
     * @param args 参数
     * @return Object[]
     */
    private Object[] argsExcludeClass(Object[] args) {
        return Stream.of(args)
                .filter(f -> !(f instanceof HttpServletResponse))
                .filter(f -> !(f instanceof HttpServletRequest))
                .filter(f -> !(f instanceof MultipartFile))
                .filter(f -> !(f instanceof Exception))
                .toArray();
    }


}
