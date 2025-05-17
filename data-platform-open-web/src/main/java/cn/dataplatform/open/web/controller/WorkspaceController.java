package cn.dataplatform.open.web.controller;

import cn.dataplatform.open.common.vo.base.IdRequest;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.common.vo.base.PlainResult;
import cn.dataplatform.open.web.annotation.Auth;
import cn.dataplatform.open.web.service.WorkspaceService;
import cn.dataplatform.open.web.vo.workspace.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("workspace")
public class WorkspaceController {

    @Resource
    private WorkspaceService workspaceService;


    /**
     * 列表
     *
     * @param pageRequest 分页请求
     * @return user
     */
    @Auth("system:workspace:list")
    @PostMapping("list")
    public PageResult<WorkspaceListResponse> list(@RequestBody @Valid PageRequest<WorkspaceListRequest> pageRequest) {
        return this.workspaceService.list(pageRequest);
    }

    /**
     * 添加
     *
     * @param workspaceAddRequest 请求
     * @return r
     */
    @Auth("system:workspace:add")
    @PostMapping("add")
    public PlainResult<Boolean> add(@RequestBody @Valid WorkspaceAddRequest workspaceAddRequest) {
        return new PlainResult<>(this.workspaceService.add(workspaceAddRequest));
    }

    /**
     * 详情
     *
     * @param idRequest id
     * @return r
     */
    @Auth("system:workspace:detail")
    @PostMapping("detail")
    public PlainResult<WorkspaceDetailResponse> detail(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.workspaceService.detail(idRequest.getId()));
    }

    /**
     * 更新
     *
     * @param workspaceUpdateRequest 请求
     * @return r
     */
    @Auth("system:workspace:update")
    @PostMapping("update")
    public PlainResult<Boolean> update(@RequestBody @Valid WorkspaceUpdateRequest workspaceUpdateRequest) {
        return new PlainResult<>(this.workspaceService.update(workspaceUpdateRequest));
    }

    /**
     * 删除
     *
     * @param idRequest id
     * @return r
     */
    @Auth("system:workspace:delete")
    @PostMapping("delete")
    public PlainResult<Boolean> delete(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.workspaceService.delete(idRequest.getId()));
    }

}
