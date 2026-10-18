package cn.dataplatform.open.web.controller.source;

import cn.dataplatform.open.common.vo.base.IdRequest;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.common.vo.base.PlainResult;
import cn.dataplatform.open.web.annotation.Auth;
import cn.dataplatform.open.web.annotation.DataPermission;
import cn.dataplatform.open.web.annotation.RateLimit;
import cn.dataplatform.open.web.annotation.ReSubmitLock;
import cn.dataplatform.open.web.enums.OperationPermissionType;
import cn.dataplatform.open.web.enums.RateLimitStrategy;
import cn.dataplatform.open.web.enums.RecordType;
import cn.dataplatform.open.web.service.datasource.DataSourceService;
import cn.dataplatform.open.web.vo.data.source.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@RestController
@RequestMapping("/datasource")
public class DataSourceController {

    @Resource
    private DataSourceService dataSourceService;

    /**
     * 数据源列表
     *
     * @param pageRequest p
     * @return r
     */
    @Auth("data:source:list")
    @PostMapping("list")
    public PageResult<DataSourceListResponse> list(@RequestBody @Valid PageRequest<DataSourceListRequest> pageRequest) {
        return this.dataSourceService.list(pageRequest);
    }


    /**
     * 数据源详情
     *
     * @param idRequest id
     * @return r
     */
    @Auth("data:source:detail")
    @PostMapping("detail")
    public PlainResult<DataSourceDetailResponse> detail(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.dataSourceService.detail(idRequest.getId()));
    }

    /**
     * 添加数据源
     *
     * @param dataSourceAddRequest d
     * @return r
     */
    @Auth("data:source:add")
    @ReSubmitLock
    @PostMapping("add")
    public PlainResult<Long> add(@RequestBody @Valid DataSourceAddRequest dataSourceAddRequest) {
        return new PlainResult<>(this.dataSourceService.add(dataSourceAddRequest));
    }

    /**
     * 删除数据源
     *
     * @param idRequest 数据源ID
     * @return r
     */
    @DataPermission(type = OperationPermissionType.EDIT, recordType = RecordType.DATA_SOURCE, id = "#idRequest.id")
    @ReSubmitLock
    @Auth("data:source:delete")
    @PostMapping("delete")
    public PlainResult<Boolean> delete(@RequestBody @Valid IdRequest idRequest) {
        return new PlainResult<>(this.dataSourceService.delete(idRequest.getId()));
    }

    /**
     * 修改数据源
     *
     * @param dataSourceUpdateRequest d
     * @return r
     */
    @DataPermission(type = OperationPermissionType.EDIT, recordType = RecordType.DATA_SOURCE, id = "#dataSourceUpdateRequest.id")
    @ReSubmitLock
    @Auth("data:source:update")
    @PostMapping("update")
    public PlainResult<Boolean> update(@RequestBody @Valid DataSourceUpdateRequest dataSourceUpdateRequest) {
        return new PlainResult<>(this.dataSourceService.update(dataSourceUpdateRequest));
    }

    /**
     * 测试数据源
     *
     * @param dataSourceTestRequest d
     * @return r
     */
    @RateLimit(type = RateLimitStrategy.USER, limit = 10, refreshInterval = 60)
    @Auth("data:source:test")
    @ReSubmitLock
    @PostMapping("test")
    public PlainResult<Boolean> test(@RequestBody @Valid DataSourceTestRequest dataSourceTestRequest) {
        return new PlainResult<>(this.dataSourceService.test(dataSourceTestRequest));
    }

    /**
     * 获取数据源下所有的库
     *
     * @param request id 或 code
     * @return 库名列表
     */
    @PostMapping("schema/list")
    public PlainResult<List<String>> listSchema(@RequestBody @Valid ListSchemaRequest request) {
        return new PlainResult<>(this.dataSourceService.listSchema(request));
    }

    /**
     * 获取某库下的表（支持表名模糊、limit，级联用）
     *
     * @param request id/code、schemaCode、tableCode、limit
     * @return 表列表
     */
    @PostMapping("table/list")
    public PlainResult<List<SchemaTable>> listTable(@RequestBody @Valid ListTableRequest request) {
        return new PlainResult<>(this.dataSourceService.listTable(request));
    }

    /**
     * 获取数据源表信息
     *
     * @param request id
     * @return r
     */
    @PostMapping("table/detail")
    public PlainResult<TableDetail> tableDetail(@RequestBody @Valid TableDetailRequest request) {
        return new PlainResult<>(this.dataSourceService.tableDetail(request));
    }

}
