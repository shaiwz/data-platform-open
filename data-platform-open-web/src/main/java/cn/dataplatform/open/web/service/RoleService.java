package cn.dataplatform.open.web.service;

import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.store.entity.Role;
import cn.dataplatform.open.web.vo.role.*;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author DaoDao
 */
public interface RoleService extends IService<Role> {

    /**
     * 列表
     *
     * @param pageRequest 分页请求
     * @return user
     */
    PageResult<RoleListResponse> list(PageRequest<RoleListRequest> pageRequest);

    /**
     * 详情
     *
     * @param id id
     * @return r
     */
    RoleDetailResponse detail(Long id);

    /**
     * 添加
     *
     * @param roleAddRequest 请求
     * @return r
     */
    Boolean add(RoleAddRequest roleAddRequest);

    /**
     * 更新
     *
     * @param roleUpdateRequest 请求
     * @return r
     */
    Boolean update(RoleUpdateRequest roleUpdateRequest);

    /**
     * 删除
     *
     * @param id id
     * @return r
     */
    Boolean delete(Long id);

}
