package cn.dataplatform.open.web.controller;

import cn.dataplatform.open.common.vo.base.IdRequest;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.common.vo.base.PlainResult;
import cn.dataplatform.open.web.annotation.Auth;
import cn.dataplatform.open.web.service.UserLoginLogService;
import cn.dataplatform.open.web.vo.login.log.LoginLogDetailResponse;
import cn.dataplatform.open.web.vo.login.log.LoginLogListRequest;
import cn.dataplatform.open.web.vo.login.log.LoginLogListResponse;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/9
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user/login/log")
public class UserLoginLogController {

    @Resource
    private UserLoginLogService userLoginLogService;

    /**
     * 登录日志列表
     *
     * @param pageRequest p
     * @return r
     */
    @Auth("security-audit:login-log:list")
    @PostMapping("list")
    public PageResult<LoginLogListResponse> list(@RequestBody @Valid PageRequest<LoginLogListRequest> pageRequest) {
        return this.userLoginLogService.list(pageRequest);
    }

    /**
     * 删除
     *
     * @param request r
     * @return r
     */
    @Auth("security-audit:login-log:delete")
    @PostMapping("delete")
    public PlainResult<Boolean> delete(@RequestBody @Valid IdRequest request) {
        Boolean boo = this.userLoginLogService.delete(request.getId());
        return new PlainResult<>(boo);
    }

    /**
     * 详情
     *
     * @param request r
     * @return r
     */
    @PostMapping("detail")
    public PlainResult<LoginLogDetailResponse> detail(@RequestBody @Valid IdRequest request) {
        return new PlainResult<>(userLoginLogService.detail(request.getId()));
    }

}
