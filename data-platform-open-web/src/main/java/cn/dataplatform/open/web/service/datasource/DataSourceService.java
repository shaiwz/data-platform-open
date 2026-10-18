package cn.dataplatform.open.web.service.datasource;


import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.store.entity.DataSource;
import cn.dataplatform.open.web.vo.data.source.*;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
public interface DataSourceService extends IService<DataSource> {

    /**
     * 数据源列表
     *
     * @param pageRequest p
     * @return r
     */
    PageResult<DataSourceListResponse> list(PageRequest<DataSourceListRequest> pageRequest);

    /**
     * 添加数据源
     *
     * @param dataSourceAddRequest d
     * @return r
     */
    Long add(DataSourceAddRequest dataSourceAddRequest);

    /**
     * 修改数据源
     *
     * @param dataSourceUpdateRequest d
     * @return r
     */
    Boolean update(DataSourceUpdateRequest dataSourceUpdateRequest);

    /**
     * 获取数据源下所有的库
     *
     * @param request id 或 code
     * @return 库名列表
     */
    List<String> listSchema(ListSchemaRequest request);

    /**
     * 获取数据源下所有的库
     *
     * @param dataSource 数据源
     * @return 库名列表
     */
    List<String> listSchema(DataSource dataSource);


    /**
     * 获取某库下的表（支持表名模糊、limit）
     *
     * @param request id/code、schemaCode、tableCode、limit
     * @return 表列表
     */
    List<SchemaTable> listTable(ListTableRequest request);

    /**
     * 获取某库下的表（支持表名模糊、limit）
     *
     * @param dataSource 数据源
     * @param schemaCode 库名
     * @param tableCode  表名（模糊）
     * @param limit      限制返回的表数量
     * @return 表列表
     */
    List<SchemaTable> listTable(DataSource dataSource, String schemaCode, String tableCode, Integer limit);

    /**
     * 删除数据源
     *
     * @param id 数据源ID
     * @return r
     */
    Boolean delete(Long id);

    /**
     * 测试数据源
     *
     * @param dataSourceTestRequest d
     * @return r
     */
    Boolean test(@Valid DataSourceTestRequest dataSourceTestRequest);

    /**
     * 数据源详情
     *
     * @param id id
     * @return r
     */
    DataSourceDetailResponse detail(Long id);

    /**
     * 数据源连接
     *
     * @param id 数据源ID
     * @return r
     */
    <T> T dataSourceConnect(Long id, Class<T> clazz);

    /**
     * 获取连接,先从缓存获取
     *
     * @param dataSource 数据库连接配置
     * @return 数据源
     */
    <T> T dataSourceConnect(DataSource dataSource, Class<T> clazz);


    /**
     * 获取数据源表信息
     *
     * @param dataSource 数据源
     * @param schema     库
     * @param table      表
     * @return r
     */
    TableDetail tableDetail(DataSource dataSource, String schema, String table);

    /**
     * 获取数据源表信息
     *
     * @param request id
     * @return r
     */
    TableDetail tableDetail(TableDetailRequest request);

}
