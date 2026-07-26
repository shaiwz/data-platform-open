package cn.dataplatform.open.common.util;

import cn.dataplatform.open.common.exception.ApiException;
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
        return HttpServletUtils.getServletRequestAttributes().getRequest();
    }

    /**
     * 获取当前请求的Response
     *
     * @return HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        return HttpServletUtils.getServletRequestAttributes().getResponse();
    }

    /**
     * 获取当前request
     *
     * @return String
     */
    private static ServletRequestAttributes getServletRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            throw new ApiException("无法获取当前请求的 RequestAttributes");
        }
        return servletRequestAttributes;
    }


}
