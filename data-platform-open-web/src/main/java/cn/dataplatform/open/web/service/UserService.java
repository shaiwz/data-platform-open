package cn.dataplatform.open.web.service;


import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.store.entity.User;
import cn.dataplatform.open.web.vo.user.*;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.Map;

/**
 * @author DaoDao
 */
public interface UserService extends IService<User> {

    /**
     * 列表
     *
     * @param pageRequest 分页请求
     * @return user
     */
    PageResult<UserListResponse> list(PageRequest<UserListRequest> pageRequest);

    /**
     * 详情
     *
     * @param id id
     * @return r
     */
    UserDetailResponse detail(Long id);

    /**
     * 添加
     *
     * @param userAddRequest 请求
     * @return r
     */
    Boolean add(UserAddRequest userAddRequest);

    /**
     * 更新
     *
     * @param userUpdateRequest 请求
     * @return r
     */
    Boolean update(UserUpdateRequest userUpdateRequest);

    /**
     * 删除
     *
     * @param id id
     * @return r
     */
    Boolean delete(Long id);

    /**
     * 重置密码
     *
     * @param request 请求
     * @return r
     */
    Boolean resetPassword(@Valid ResetPasswordRequest request);

    /**
     * 修改密码
     *
     * @param request 请求
     * @return r
     */
    Boolean changePassword(@Valid ChangePasswordRequest request);


    /**
     * 根据id列表获取用户列表
     *
     * @param ids id列表
     * @return 用户列表
     */
    Map<Long, User> getAllUserMapByIds(Collection<Long> ids);

}
