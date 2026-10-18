package cn.dataplatform.open.support.service;

import cn.dataplatform.open.support.store.entity.DataSource;
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
     * @param code 数据源Code
     */
    void load(String workspaceCode, String code);

    /**
     * 移除数据源
     *
     * @param code 数据源Code
     */
    void remove(String workspaceCode, String code);

}