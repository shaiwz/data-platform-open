package cn.dataplatform.open.flow.service;

import cn.dataplatform.open.flow.store.entity.DataSource;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/11
 * @since 1.0.0
 */
public interface DataSourceService extends IService<DataSource> {

    /**
     * 加载数据源
     *
     * @param dataSourceCode 数据源编码
     */
    void load(String workspaceCode, String dataSourceCode);

    /**
     * 移除数据源
     *
     * @param dataSourceCode 数据源编码
     */
    void remove(String workspaceCode, String dataSourceCode);

}
