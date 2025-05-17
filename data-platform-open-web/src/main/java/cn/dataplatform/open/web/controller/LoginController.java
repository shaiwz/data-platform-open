package cn.dataplatform.open.web.controller;

import cn.dataplatform.open.common.vo.base.PlainResult;
import cn.dataplatform.open.web.annotation.RateLimit;
import cn.dataplatform.open.web.service.LoginService;
import cn.dataplatform.open.web.vo.login.LoginRequest;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/19
 * @since 1.0.0
 */
@RestController
public class LoginController {

    @Resource
    private LoginService loginService;

    /**
     * 登录
     *
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    @RateLimit(limit = 10, refreshInterval = 60)
    @PostMapping("login")
    public PlainResult<Object> login(@RequestBody @Valid LoginRequest loginRequest) {
        return new PlainResult<>(loginService.login(loginRequest));
    }

    /**
     * 登出
     *
     * @return 登出结果
     */
    @PostMapping("logout")
    public PlainResult<Object> logout() {
        return new PlainResult<>(loginService.logout());
    }

}
