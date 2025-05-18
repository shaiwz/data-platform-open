package cn.dataplatform.open.web.interceptor;


import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.web.config.Context;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@Slf4j
@Component
public class TraceInterceptor implements AsyncHandlerInterceptor {


    /**
     * 处理requestId
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return 是否继续处理请求
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 如果上游系统传入requestId则使用上游系统的requestId
        String requestId = request.getHeader(Constant.REQUEST_ID);
        if (StrUtil.isEmpty(requestId)) {
            // 否则生成一个
            requestId = UUID.randomUUID().toString(true);
        }
        MDC.put(Constant.REQUEST_ID, requestId);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception exception) {
        MDC.clear();
        Context.clearAll();
    }

    /**
     * 获取request id
     *
     * @return request id
     */
    public static String getRequestId() {
        return MDC.get(Constant.REQUEST_ID);
    }

}

