package cn.dataplatform.open.web.service;


import cn.dataplatform.open.web.store.entity.UserRole;
import cn.dataplatform.open.web.vo.role.RoleDetailResponse;
import cn.dataplatform.open.web.vo.role.UserRoleUpsertRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author DaoDao
 */
public interface UserRoleService extends IService<UserRole> {

    /**
     * 用户的角色列表
     *
     * @return r
     */
    List<RoleDetailResponse> list(Long userId);

    /**
     * 我的角色列表
     *
     * @return r
     */
    List<RoleDetailResponse> my();

    /**
     * 更新用户角色
     *
     * @param userRoleUpsertRequest 请求
     * @return r
     */
    Boolean upsert(UserRoleUpsertRequest userRoleUpsertRequest);

}
