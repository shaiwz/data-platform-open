package cn.dataplatform.open.web.service;

import cn.dataplatform.open.web.vo.login.LoginRequest;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/19
 * @since 1.0.0
 */
public interface LoginService {

    /**
     * 登录
     *
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    Object login(LoginRequest loginRequest);

    /**
     * 登出
     *
     * @return 登出结果
     */
    Object logout();

}
