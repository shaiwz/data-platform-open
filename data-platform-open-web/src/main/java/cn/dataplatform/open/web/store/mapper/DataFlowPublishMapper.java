package cn.dataplatform.open.web.store.mapper;

import cn.dataplatform.open.web.store.entity.DataFlowPublish;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/22
 * @since 1.0.0
 */
public interface DataFlowPublishMapper extends BaseMapper<DataFlowPublish> {

    /**
     * 查询这个数据源是否有被数据流引用
     *
     * @param workspaceCode 工作空间编码
     * @param code          数据源编码
     * @return 数据流
     */
    @Select("SELECT * FROM data_flow_publish WHERE" +
            " workspace_code = #{workspaceCode} and JSON_CONTAINS(datasource_codes, JSON_ARRAY(#{code})) limit 1")
    DataFlowPublish refDataSourceCode(@Param("workspaceCode") String workspaceCode, @Param("code") String code);


}
