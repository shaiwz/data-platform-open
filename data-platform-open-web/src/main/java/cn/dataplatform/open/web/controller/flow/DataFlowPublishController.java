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
import cn.dataplatform.open.web.service.flow.DataFlowPublishService;
import cn.dataplatform.open.web.vo.data.flow.publish.DataFlowPublishDetailResponse;
import cn.dataplatform.open.web.vo.data.flow.publish.DataFlowPublishListResponse;
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
 * @date 2025/2/19
 * @since 1.0.0
 */
@RestController
@RequestMapping("/dataflow/publish")
public class DataFlowPublishController {

    @Resource
    private DataFlowPublishService dataFlowPublishService;

    /**
     * 历史版本
     *
     * @param pageRequest p
     * @return r
     */
    @PostMapping("historyList")
    public PageResult<DataFlowPublishListResponse> historyList(@RequestBody @Valid PageRequest<String> pageRequest) {
        return this.dataFlowPublishService.historyList(pageRequest);
    }


    /**
     * 删除
     *
     * @param idRequest id
     * @return r
     */
    @Auth("data:flow:delete")
    @DataPermission(type = OperationPermissionType.EDIT, recordType = RecordType.DATA_FLOW, id = "#idRequest.id")
    @ReSubmitLock
    @PostMapping("delete")
    public PlainResult<Boolean> delete(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.dataFlowPublishService.delete(idRequest.getId()));
    }

    /**
     * 获取已发布数据流详情
     *
     * @param idRequest d
     * @return r
     */
    @Auth("data:flow:detail")
    @PostMapping("detail")
    public PlainResult<DataFlowPublishDetailResponse> detail(@RequestBody @Valid IdRequest idRequest) {
        DataFlowPublishDetailResponse dataFlowPublishDetailResponse = this.dataFlowPublishService.detail(idRequest.getId());
        return new PlainResult<>(dataFlowPublishDetailResponse);
    }

}
