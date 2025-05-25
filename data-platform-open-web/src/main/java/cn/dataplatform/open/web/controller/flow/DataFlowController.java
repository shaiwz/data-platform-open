package cn.dataplatform.open.web.controller.flow;

import cn.dataplatform.open.common.vo.base.IdRequest;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.common.vo.base.PlainResult;
import cn.dataplatform.open.web.annotation.Auth;
import cn.dataplatform.open.web.annotation.DataPermission;
import cn.dataplatform.open.web.annotation.ReSubmitLock;
import cn.dataplatform.open.web.enums.OperationPermissionType;
import cn.dataplatform.open.web.enums.RecordType;
import cn.dataplatform.open.web.service.flow.DataFlowService;
import cn.dataplatform.open.web.vo.data.flow.*;
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
 * @date 2025/1/4
 * @since 1.0.0
 */
@RestController
@RequestMapping("/dataflow")
public class DataFlowController {

    @Resource
    private DataFlowService dataFlowService;

    /**
     * 数据流列表
     *
     * @param pageRequest p
     * @return r
     */
    @Auth("data:flow:list")
    @PostMapping("list")
    public PageResult<DataFlowListResponse> list(@RequestBody @Valid PageRequest<DataFlowListRequest> pageRequest) {
        return this.dataFlowService.list(pageRequest);
    }


    /**
     * 获取数据流详情
     *
     * @param idRequest d
     * @return r
     */
    @Auth("data:flow:detail")
    @PostMapping("detail")
    public PlainResult<DataFlowDetailResponse> detail(@RequestBody @Valid IdRequest idRequest) {
        DataFlowDetailResponse dataFlowDetailResponse = this.dataFlowService.detail(idRequest.getId());
        return new PlainResult<>(dataFlowDetailResponse);
    }


    /**
     * 创建数据流
     *
     * @param dataFlowListResponse d
     * @return r
     */
    @ReSubmitLock
    @Auth("data:flow:create")
    @PostMapping("create")
    public PlainResult<DataFlowCreateResponse> create(@RequestBody @Valid
                                                      DataFlowCreateRequest dataFlowListResponse) {
        DataFlowCreateResponse dataFlowCreateResponse = this.dataFlowService.create(dataFlowListResponse);
        return new PlainResult<>(dataFlowCreateResponse);
    }


    /**
     * 更新数据流
     *
     * @param dataFlowUpdateRequest d
     * @return r
     */
    @ReSubmitLock
    @DataPermission(type = OperationPermissionType.EDIT, recordType = RecordType.DATA_FLOW, id = "#dataFlowUpdateRequest.id")
    @Auth("data:flow:update")
    @PostMapping("update")
    public PlainResult<Boolean> update(@RequestBody @Valid
                                       DataFlowUpdateRequest dataFlowUpdateRequest) {
        Boolean update = this.dataFlowService.update(dataFlowUpdateRequest);
        return new PlainResult<>(update);
    }

    /**
     * 发布
     *
     * @param publishRequest d
     * @return r
     */
    @DataPermission(type = OperationPermissionType.PUBLISH, recordType = RecordType.DATA_FLOW, id = "#publishRequest.id")
    @ReSubmitLock
    @Auth("data:flow:publish")
    @PostMapping("publish")
    public PlainResult<Boolean> publish(@RequestBody @Valid PublishRequest publishRequest) {
        return new PlainResult<>(this.dataFlowService.publish(publishRequest));
    }


    /**
     * 停止流程
     *
     * @param idRequest d
     * @return r
     */
    @DataPermission(type = OperationPermissionType.PUBLISH, recordType = RecordType.DATA_FLOW, id = "#idRequest.id")
    @ReSubmitLock
    @Auth("data:flow:stop")
    @PostMapping("stop")
    public PlainResult<Boolean> stop(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.dataFlowService.stop(idRequest.getId()));
    }

    /**
     * 启动流程
     *
     * @param idRequest d
     * @return r
     */
    @DataPermission(type = OperationPermissionType.PUBLISH, recordType = RecordType.DATA_FLOW, id = "#idRequest.id")
    @ReSubmitLock
    @Auth("data:flow:start")
    @PostMapping("start")
    public PlainResult<Boolean> start(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.dataFlowService.start(idRequest.getId()));
    }

    /**
     * 删除流程
     *
     * @param idRequest d
     * @return r
     */
    @Auth("data:flow:delete")
    @DataPermission(type = OperationPermissionType.EDIT, recordType = RecordType.DATA_FLOW, id = "#idRequest.id")
    @ReSubmitLock
    @PostMapping("delete")
    public PlainResult<Boolean> delete(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.dataFlowService.delete(idRequest.getId()));
    }

    /**
     * 回滚至某个版本
     *
     * @param idRequest id
     * @return r
     */
    @Auth("data:flow:publish")
    @DataPermission(type = OperationPermissionType.EDIT, recordType = RecordType.DATA_FLOW, id = "#idRequest.id")
    @ReSubmitLock
    @PostMapping("rollback")
    public PlainResult<Boolean> rollback(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.dataFlowService.rollback(idRequest.getId()));
    }

    /**
     * 查询已经发布过的数据流 编码还有名称
     *
     * @param query 查询条件
     * @return r
     */
    @PostMapping("listPublished")
    public PlainResult<Object> listPublished(String query) {
        return new PlainResult<>(this.dataFlowService.listPublished(query));
    }


}
