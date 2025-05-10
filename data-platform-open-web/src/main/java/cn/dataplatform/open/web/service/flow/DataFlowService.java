package cn.dataplatform.open.web.service.flow;


import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.store.entity.DataFlow;
import cn.dataplatform.open.web.vo.data.flow.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/4
 * @since 1.0.0
 */
public interface DataFlowService extends IService<DataFlow> {

    /**
     * 数据流列表
     *
     * @param pageRequest p
     * @return r
     */
    PageResult<DataFlowListResponse> list(PageRequest<DataFlowListRequest> pageRequest);

    /**
     * 创建数据流
     *
     * @param dataFlowListResponse d
     * @return r
     */
    DataFlowCreateResponse create(DataFlowCreateRequest dataFlowListResponse);

    /**
     * 更新数据流
     *
     * @param dataFlowUpdateRequest d
     * @return r
     */
    Boolean update(DataFlowUpdateRequest dataFlowUpdateRequest);

    /**
     * 获取数据流详情
     *
     * @param id d
     * @return r
     */
    DataFlowDetailResponse detail(Long id);

    /**
     * 发布
     *
     * @param publishRequest d
     * @return r
     */
    Boolean publish(PublishRequest publishRequest);

    /**
     * 停止流程
     *
     * @param id d
     * @return r
     */
    Boolean stop(Long id);

    /**
     * 删除流程
     *
     * @param id d
     * @return r
     */
    Boolean delete(Long id);


    /**
     * 回滚至某个版本
     *
     * @param id id
     * @return r
     */
    Boolean rollback(Long id);

    /**
     * * 查询已经发布过的数据流 编码还有名称
     *
     * @return r
     */
    List<ListPublishedResponse> listPublished(String query);

    /**
     * 启动流程
     *
     * @param id d
     * @return r
     */
    Boolean start(Long id);

}
