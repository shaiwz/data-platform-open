package cn.dataplatform.open.common.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpServletUtils {

    public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

    /**
     * 获取当前请求的request
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        return getServletRequestAttributes().getRequest();
    }

    /**
     * 获取当前请求的Response
     *
     * @return HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        return getServletRequestAttributes().getResponse();
    }

    /**
     * 获取当前request
     *
     * @return String
     */
    private static ServletRequestAttributes getServletRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            throw new RuntimeException("not request");
        }
        return servletRequestAttributes;
    }

}
