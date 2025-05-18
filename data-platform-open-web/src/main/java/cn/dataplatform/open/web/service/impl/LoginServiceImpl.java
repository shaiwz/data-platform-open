package cn.dataplatform.open.web.service.impl;

import cn.dataplatform.open.common.constant.Constant;
import cn.dataplatform.open.common.enums.RedisKey;
import cn.dataplatform.open.common.enums.Status;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.common.util.HttpServletUtils;
import cn.dataplatform.open.common.util.IPUtils;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.interceptor.TokenInterceptor;
import cn.dataplatform.open.web.service.LoginService;
import cn.dataplatform.open.web.service.UserLoginLogService;
import cn.dataplatform.open.web.service.UserService;
import cn.dataplatform.open.web.store.entity.User;
import cn.dataplatform.open.web.store.entity.UserLoginLog;
import cn.dataplatform.open.web.store.mapper.UserLoginLogMapper;
import cn.dataplatform.open.web.util.JWTUtils;
import cn.dataplatform.open.web.util.MD5Utils;
import cn.dataplatform.open.web.vo.login.LoginRequest;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/19
 * @since 1.0.0
 */
@Slf4j
@Service
public class LoginServiceImpl implements LoginService {


    @Value("${dp.auth.jwt.issuer:data-platform}")
    private String issuer;
    @Value("${dp.auth.token.keep-time:3600000}")
    public Long tokenKeepTime;

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private UserService userService;
    @Resource
    private UserLoginLogMapper userLoginLogMapper;
    @Resource
    private UserLoginLogService userLoginLogService;

    /**
     * 登录
     *
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    @Override
    public Object login(LoginRequest loginRequest) {
        User user = this.userService.lambdaQuery()
                .and(a -> a.eq(User::getUsername, loginRequest.getAccount())
                        .or()
                        .eq(User::getEmail, loginRequest.getAccount())
                )
                .eq(User::getPassword, MD5Utils.encrypt(loginRequest.getPassword()))
                .one();
        if (user == null) {
            throw new ApiException("用户名或密码错误！");
        }
        // 用户是否停用
        if (Objects.equals(user.getStatus(), Status.DISABLE.name())) {
            throw new ApiException("用户已停用，请联系管理员！");
        }
        UserLoginLog loginLog = this.userLoginLogService.lambdaQuery()
                .eq(UserLoginLog::getUserId, user.getId())
                .orderByDesc(UserLoginLog::getCreateTime)
                .last(Constant.LIMIT_ONE)
                .one();
        if (loginLog != null) {
            String ip = loginLog.getIp();
            String requestIp = IPUtils.getRequestIp();
            if (!ip.equals(requestIp)) {
                log.warn("用户登录IP异常,上次登录IP:{},本次登录IP:{}", ip, requestIp);
                // 后续改为手机验证码方式二次确认？
            }
        }
        String token = JWTUtils.genderToken(String.valueOf(user.getId()), this.issuer, user.getUsername());
        // set redis
        RBucket<UserData> bucket = this.redissonClient.getBucket(RedisKey.TOKEN.build(token));
        //保存到redis,用户访问时获取
        UserData userData = new UserData();
        BeanUtil.copyProperties(user, userData);
        bucket.set(userData, Duration.ofMillis(this.tokenKeepTime));
        // 记录登录日志
        HttpServletRequest request = HttpServletUtils.getRequest();
        String agent = request.getHeader(Header.USER_AGENT.toString());
        UserLoginLog userLoginLog = new UserLoginLog();
        userLoginLog.setRequestId(MDC.get(Constant.REQUEST_ID));
        userLoginLog.setUserId(user.getId());
        userLoginLog.setUsername(user.getUsername());
        // 应该还需要记录ip所在省市区,如果与之前登录差距较大,需要发送通知给用户
        userLoginLog.setIp(IPUtils.getRequestIp());
        UserAgent userAgent = UserAgentUtil.parse(agent);
        if (userAgent != null) {
            // 浏览器
            userLoginLog.setBrowser(userAgent.getBrowser().getName());
            // 系统
            userLoginLog.setOs(userAgent.getOs().getName());
            userLoginLog.setPlatform(userAgent.getPlatform().getName());
        }
        userLoginLog.setUserAgent(StrUtil.maxLength(agent, 2000));
        userLoginLog.setCreateTime(LocalDateTime.now());
        this.userLoginLogMapper.insert(userLoginLog);
        // 维护用户id与token的关系
        RMapCache<Long, String> mapCache = this.redissonClient.getMapCache(RedisKey.USER_TOKEN.getKey());
        // 原来的token强制下线处理
        String oldToken = mapCache.get(user.getId());
        if (oldToken != null) {
            RBucket<UserData> oldBucket = this.redissonClient.getBucket(RedisKey.TOKEN.build(oldToken));
            // 如果需要强制下线其他登录会话，打开以下限制，则限制用户只能在一个地方登录
            // oldBucket.delete();
        }
        // 保存新的token关系
        mapCache.put(user.getId(), token, 10, TimeUnit.DAYS);
        return token;
    }

    /**
     * 登出
     *
     * @return 登出结果
     */
    @Override
    public Object logout() {
        String token = HttpServletUtils.getRequest().getHeader(TokenInterceptor.AUTHORIZATION);
        if (token == null) {
            return true;
        }
        UserData user = Context.getUser();
        // 删除用户id与token的关系
        RMapCache<Long, String> mapCache = this.redissonClient.getMapCache(RedisKey.USER_TOKEN.getKey());
        mapCache.put(user.getId(), token, 10, TimeUnit.DAYS);
        RBucket<UserData> bucket = this.redissonClient.getBucket(RedisKey.TOKEN.build(token));
        return bucket.delete();
    }

}
