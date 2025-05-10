package cn.dataplatform.open.web.service.impl;


import cn.dataplatform.open.common.component.OrikaMapper;
import cn.dataplatform.open.common.vo.base.PageBase;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.service.UserLoginLogService;
import cn.dataplatform.open.web.service.UserService;
import cn.dataplatform.open.web.store.entity.User;
import cn.dataplatform.open.web.store.entity.UserLoginLog;
import cn.dataplatform.open.web.store.mapper.UserLoginLogMapper;
import cn.dataplatform.open.web.vo.login.log.LoginLogDetailResponse;
import cn.dataplatform.open.web.vo.login.log.LoginLogListRequest;
import cn.dataplatform.open.web.vo.login.log.LoginLogListResponse;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/9
 * @since 1.0.0
 */
@Service
public class UserLoginLogServiceImpl extends ServiceImpl<UserLoginLogMapper, UserLoginLog>
        implements UserLoginLogService {

    @Resource
    private OrikaMapper orikaMapper;
    @Resource
    private UserService userService;

    /**
     * 登录日志列表
     *
     * @param pageRequest p
     * @return r
     */
    @Override
    public PageResult<LoginLogListResponse> list(PageRequest<LoginLogListRequest> pageRequest) {
        PageBase page = pageRequest.getPage();
        LoginLogListRequest query = pageRequest.getQuery();
        Page<UserLoginLog> paged = this.lambdaQuery()
                .like(StrUtil.isNotBlank(query.getUsername()), UserLoginLog::getUsername, query.getUsername())
                .eq(StrUtil.isNotBlank(query.getRequestId()), UserLoginLog::getRequestId, query.getRequestId())
                .eq(query.getUserId() != null, UserLoginLog::getUserId, query.getUserId())
                .eq(StrUtil.isNotBlank(query.getIp()), UserLoginLog::getIp, query.getIp())
                // time
                .ge(query.getStartCreateTime() != null, UserLoginLog::getCreateTime, query.getStartCreateTime())
                .le(query.getEndCreateTime() != null, UserLoginLog::getCreateTime, query.getEndCreateTime())
                .orderByDesc(UserLoginLog::getCreateTime)
                .page(new Page<>(page.getCurrent(), page.getSize()));
        List<UserLoginLog> records = paged.getRecords();
        if (records.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), paged.getCurrent(), paged.getSize(), paged.getTotal());
        }
        Set<Long> userIds = records.stream().map(UserLoginLog::getUserId).collect(Collectors.toSet());
        List<User> users = this.userService.lambdaQuery()
                .in(User::getId, userIds)
                .list();
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        PageResult<LoginLogListResponse> pageResult = new PageResult<>();
        List<LoginLogListResponse> collect = records.stream()
                .map(m -> {
                    LoginLogListResponse listResponse = new LoginLogListResponse();
                    this.orikaMapper.map(m, listResponse);
                    User user = userMap.get(m.getUserId());
                    if (user != null) {
                        UserData userData = this.orikaMapper.map(user, UserData.class);
                        listResponse.setUser(userData);
                    }
                    return listResponse;
                }).collect(Collectors.toList());
        pageResult.setData(collect, paged.getCurrent(), paged.getSize(), paged.getTotal());
        return pageResult;
    }


    /**
     * 删除
     *
     * @param id r
     * @return r
     */
    @Override
    public Boolean delete(Long id) {
        return this.removeById(id);
    }

    /**
     * 详情
     *
     * @param id r
     * @return r
     */
    @Override
    public LoginLogDetailResponse detail(Long id) {
        UserLoginLog userLoginLog = this.getById(id);
        if (userLoginLog != null) {
            LoginLogDetailResponse detailResponse = new LoginLogDetailResponse();
            this.orikaMapper.map(userLoginLog, detailResponse);
            return detailResponse;
        }
        return null;
    }


}
