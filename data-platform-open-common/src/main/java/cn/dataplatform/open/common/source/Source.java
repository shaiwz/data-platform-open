package cn.dataplatform.open.common.source;

import cn.dataplatform.open.common.enums.DataSourceType;

import java.io.Closeable;

/**
 * 〈Source〉
 *
 * @author dqw
 * @since 1.0.0
 */
public interface Source extends Closeable {

    /**
     * 编码
     *
     * @return 编码
     */
    String code();

    /**
     * 名称
     *
     * @return 名称
     */
    String name();

    /**
     * 类型
     *
     * @return 类型
     */
    DataSourceType type();

    /**
     * 是否启用健康检查
     *
     * @return true启用
     */
    Boolean isEnableHealth();

    /**
     * 健康检查
     *
     * @return true健康
     */
    Boolean health() throws Exception;

}