package cn.dataplatform.open.web.service;

import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.store.entity.Permission;
import cn.dataplatform.open.web.vo.permission.*;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author DaoDao
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 列表
     *
     * @param pageRequest 分页请求
     * @return user
     */
    PageResult<PermissionListResponse> list(PageRequest<PermissionListRequest> pageRequest);

    /**
     * 详情
     *
     * @param id id
     * @return r
     */
    PermissionDetailResponse detail(Long id);

    /**
     * 添加
     *
     * @param permissionAddRequest 请求
     * @return r
     */
    Boolean add(PermissionAddRequest permissionAddRequest);

    /**
     * 更新
     *
     * @param permissionUpdateRequest 请求
     * @return r
     */
    Boolean update(PermissionUpdateRequest permissionUpdateRequest);

    /**
     * 删除
     *
     * @param id id
     * @return r
     */
    Boolean delete(Long id);

}
