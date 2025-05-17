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
     * @param id 数据源ID
     */
    void load(Long id);

    /**
     * 移除数据源
     *
     * @param id 数据源ID
     */
    void remove(Long id);

}
