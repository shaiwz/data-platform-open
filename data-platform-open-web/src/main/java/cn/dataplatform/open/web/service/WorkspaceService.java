package cn.dataplatform.open.web.service;


import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.store.entity.Workspace;
import cn.dataplatform.open.web.vo.workspace.*;
import com.baomidou.mybatisplus.extension.service.IService;

public interface WorkspaceService extends IService<Workspace> {

    /**
     * 列表
     *
     * @param pageRequest 分页请求
     * @return user
     */
    PageResult<WorkspaceListResponse> list(PageRequest<WorkspaceListRequest> pageRequest);

    /**
     * 详情
     *
     * @param id id
     * @return r
     */
    WorkspaceDetailResponse detail(Long id);

    /**
     * 添加
     *
     * @param workspaceAddRequest 请求
     * @return r
     */
    Boolean add(WorkspaceAddRequest workspaceAddRequest);

    /**
     * 更新
     *
     * @param workspaceUpdateRequest 请求
     * @return r
     */
    Boolean update(WorkspaceUpdateRequest workspaceUpdateRequest);

    /**
     * 删除
     *
     * @param id id
     * @return r
     */
    Boolean delete(Long id);

}
