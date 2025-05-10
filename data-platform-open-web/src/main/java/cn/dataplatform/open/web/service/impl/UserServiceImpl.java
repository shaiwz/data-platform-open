package cn.dataplatform.open.web.service.impl;


import cn.dataplatform.open.common.component.OrikaMapper;
import cn.dataplatform.open.common.enums.RedisKey;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.common.util.HttpServletUtils;
import cn.dataplatform.open.common.vo.base.PageBase;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.service.UserService;
import cn.dataplatform.open.web.service.UserWorkspaceService;
import cn.dataplatform.open.web.store.entity.User;
import cn.dataplatform.open.web.store.entity.UserWorkspace;
import cn.dataplatform.open.web.store.mapper.UserMapper;
import cn.dataplatform.open.web.util.MD5Utils;
import cn.dataplatform.open.web.vo.user.*;
import cn.dataplatform.open.web.vo.workspace.WorkspaceData;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.redisson.api.RBucket;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/19
 * @since 1.0.0
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${dp.auth.token.keep-time:3600000}")
    public Long tokenKeepTime;

    @Resource
    private OrikaMapper orikaMapper;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private UserWorkspaceService userWorkspaceService;

    /**
     * 列表
     *
     * @param pageRequest 分页请求
     * @return user
     */
    @Override
    public PageResult<UserListResponse> list(PageRequest<UserListRequest> pageRequest) {
        PageBase page = pageRequest.getPage();
        UserListRequest query = Optional.ofNullable(pageRequest.getQuery()).orElse(new UserListRequest());
        Page<User> userPage = this.lambdaQuery()
                .like(StrUtil.isNotBlank(query.getUsername()), User::getUsername, query.getUsername())
                .eq(StrUtil.isNotBlank(query.getEmail()), User::getEmail, query.getEmail())
                .eq(query.getStatus() != null, User::getStatus, query.getStatus())
                // 创建时间早的在前面
                .orderByAsc(User::getCreateTime)
                .page(new Page<>(page.getCurrent(), page.getSize()));
        PageResult<UserListResponse> pageResult = new PageResult<>();
        List<User> records = userPage.getRecords();
        if (records.isEmpty()) {
            pageResult.setData(Collections.emptyList(), userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
            return pageResult;
        }
        RMapCache<Long, String> mapCache = this.redissonClient.getMapCache(RedisKey.USER_TOKEN.getKey());
        List<UserListResponse> collect = records.stream()
                .map(m -> {
                    UserListResponse userListResponse = new UserListResponse();
                    this.orikaMapper.map(m, userListResponse);
                    String token = mapCache.get(m.getId());
                    if (StrUtil.isNotBlank(token)) {
                        // 查询这个token是否还在
                        RBucket<UserData> bucket = this.redissonClient.getBucket(RedisKey.TOKEN.build(token));
                        userListResponse.setOnline(bucket.isExists());
                    } else {
                        userListResponse.setOnline(false);
                    }
                    return userListResponse;
                }).collect(Collectors.toList());
        pageResult.setData(collect, userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        return pageResult;
    }

    /**
     * 详情
     *
     * @param id id
     * @return r
     */
    @Override
    public UserDetailResponse detail(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new ApiException("用户不存在");
        }
        UserDetailResponse userDetailResponse = new UserDetailResponse();
        this.orikaMapper.map(user, userDetailResponse);
        return userDetailResponse;
    }

    /**
     * 添加
     *
     * @param userAddRequest 请求
     * @return r
     */
    @Override
    public Boolean add(UserAddRequest userAddRequest) {
        UserData userData = Context.getUser();
        // 只能超级管理员可以添加用户
        if (!userData.isAdmin()) {
            throw new ApiException("只有超级管理员可以添加用户");
        }
        // 用户名是否已经存在
        boolean exists = this.lambdaQuery()
                .eq(User::getUsername, userAddRequest.getUsername())
                .exists();
        if (exists) {
            throw new ApiException("用户名已存在");
        }
        // 邮箱是否存在
        if (this.lambdaQuery().eq(User::getEmail, userAddRequest.getEmail())
                .exists()) {
            throw new ApiException("邮箱已存在");
        }
        User user = new User();
        this.orikaMapper.map(userAddRequest, user);
        /*
         * 如果密码为空,则默认为使用用户名作为密码
         */
        String encryptedPassword = MD5Utils.encrypt(userAddRequest.getPassword());
        user.setPassword(encryptedPassword);
        user.setCreateUserId(userData.getId());
        user.setDescription("这个人很懒,什么都没有留下");
        this.save(user);
        /*
         * 添加用户到工作空间
         */
        WorkspaceData workspace = Context.getWorkspace();
        if (workspace != null) {
            UserWorkspace userWorkspace = new UserWorkspace();
            userWorkspace.setUserId(user.getId());
            userWorkspace.setWorkspaceId(workspace.getId());
            userWorkspace.setCreateUserId(userData.getId());
            // 默认普通用户
            userWorkspace.setIsAdmin(0);
            this.userWorkspaceService.save(userWorkspace);
        }
        return true;
    }

    /**
     * 更新
     *
     * @param userUpdateRequest 请求
     * @return r
     */
    @Override
    public Boolean update(UserUpdateRequest userUpdateRequest) {
        UserData userData = Context.getUser();
        // 如果当前用户不是admin，只能更新自己的数据
        if (!userData.isAdmin() && !Objects.equals(userData.getId(), userUpdateRequest.getId())) {
            throw new ApiException("无权限更新用户");
        }
        // 邮箱是否存在
        boolean exists = this.lambdaQuery()
                .eq(User::getEmail, userUpdateRequest.getEmail())
                .ne(User::getId, userUpdateRequest.getId())
                .exists();
        if (exists) {
            throw new ApiException("邮箱已存在");
        }
        Long id = userUpdateRequest.getId();
        User user = this.getById(id);
        this.orikaMapper.map(userUpdateRequest, user);
        String password = userUpdateRequest.getPassword();
        if (null != password && !password.isEmpty()) {
            String encryptedPassword = MD5Utils.encrypt(password);
            user.setPassword(encryptedPassword);
        }
        this.updateById(user);
        if (Objects.equals(userData.getId(), id)) {
            // 更新自己
            String authorization = HttpServletUtils.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            RBucket<UserData> bucket = this.redissonClient.getBucket(RedisKey.TOKEN.build(authorization));
            this.orikaMapper.map(user, userData);
            bucket.set(userData, Duration.ofMillis(this.tokenKeepTime));
        }
        return true;
    }

    /**
     * 删除
     *
     * @param id id
     * @return r
     */
    @Override
    public Boolean delete(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new ApiException("用户不存在");
        }
        this.removeById(id);
        return true;
    }

    /**
     * 重置密码
     *
     * @param request 请求
     * @return r
     */
    @Override
    public Boolean resetPassword(ResetPasswordRequest request) {
        Long id = request.getId();
        // 如果不是管理员，只能重置自己的密码
        UserData userData = Context.getUser();
        if (!userData.isAdmin() && !Objects.equals(userData.getId(), id)) {
            throw new ApiException("无权限重置用户密码");
        }
        String password = request.getPassword();
        User user = this.getById(id);
        if (user == null) {
            throw new ApiException("用户不存在");
        }
        String encryptedPassword = MD5Utils.encrypt(password);
        user.setPassword(encryptedPassword);
        this.updateById(user);
        return true;
    }

    /**
     * 修改密码
     *
     * @param request 请求
     * @return r
     */
    @Override
    public Boolean changePassword(ChangePasswordRequest request) {
        UserData userData = Context.getUser();
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();
        if (oldPassword.equals(newPassword)) {
            throw new ApiException("新密码不能和旧密码相同");
        }
        User user = this.getById(userData.getId());
        if (user == null) {
            throw new ApiException("用户不存在");
        }
        String encryptedOldPassword = MD5Utils.encrypt(oldPassword);
        if (!user.getPassword().equals(encryptedOldPassword)) {
            throw new ApiException("旧密码错误");
        }
        String encryptedNewPassword = MD5Utils.encrypt(newPassword);
        user.setPassword(encryptedNewPassword);
        this.updateById(user);
        return true;
    }


}
