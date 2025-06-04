package cn.dataplatform.open.web.service;

import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.store.entity.OperationLog;
import cn.dataplatform.open.web.vo.operation.log.OperationLogDetailResponse;
import cn.dataplatform.open.web.vo.operation.log.OperationLogListRequest;
import cn.dataplatform.open.web.vo.operation.log.OperationLogListResponse;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/9
 * @since 1.0.0
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 操作日志列表
     *
     * @param pageRequest p
     * @return r
     */
    PageResult<OperationLogListResponse> list(PageRequest<OperationLogListRequest> pageRequest);

    /**
     * 删除
     *
     * @param id r
     * @return r
     */
    Boolean delete(Long id);


    /**
     * 详情
     *
     * @param id r
     * @return r
     */
    OperationLogDetailResponse detail(Long id);
}
