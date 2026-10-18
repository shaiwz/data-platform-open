package cn.dataplatform.open.common.source;

import cn.hutool.core.io.IoUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/5
 * @since 1.0.0
 */
@Getter
@Slf4j
@Component
public class SourceManager implements Closeable {

    /**
     * key为工作空间编码 value-key数据源编码,value-value连接信息
     */
    private final Map<String, Map<String, Source>> sources = new ConcurrentHashMap<>();


    public SourceManager() {
    }

    /**
     * 获取数据源
     *
     * @param workspace      工作空间
     * @param datasourceCode 数据源编码
     * @return Source
     */
    public Source get(String workspace, String datasourceCode) {
        Map<String, Source> sourceMap = this.sources.get(workspace);
        if (sourceMap == null) {
            return null;
        }
        return sourceMap.get(datasourceCode);
    }

    /**
     * 获取指定类型的数据源，如果不是则报错
     *
     * @param workspace      工作空间
     * @param datasourceCode 数据源编码
     * @param tClass         类型
     * @return 数据源
     */
    public <T> T get(String workspace, String datasourceCode, Class<T> tClass) {
        Source source = this.get(workspace, datasourceCode);
        if (source == null) {
            throw new IllegalStateException("数据源不存在:" + datasourceCode);
        }
        if (tClass.isInstance(source)) {
            return tClass.cast(source);
        } else {
            // 数据源类型不匹配
            throw new IllegalStateException("数据源类型不匹配,期望类型:" + tClass.getName() + ",实际类型:" + source.getClass().getName());
        }
    }

    /**
     * 添加数据源
     *
     * @param workspace 工作空间
     * @param source    数据源
     */
    public synchronized void add(String workspace, Source source) {
        Source originSource = this.get(workspace, source.code());
        // put
        Map<String, Source> sourceMap = this.sources.computeIfAbsent(workspace, (key) -> new ConcurrentHashMap<>());
        sourceMap.put(source.code(), source);
        if (originSource != null) {
            // 关闭原数据源
            IoUtil.close(originSource);
        }
    }

    /**
     * 移除数据源
     *
     * @param workspace      工作空间
     * @param datasourceCode 数据源编码
     * @return Source
     */
    public Source remove(String workspace, String datasourceCode) {
        Map<String, Source> sourceMap = this.sources.get(workspace);
        if (sourceMap == null) {
            return null;
        }
        return sourceMap.remove(datasourceCode);
    }

    /**
     * 关闭所有数据源
     */
    @Override
    public void close() {
        log.info("开始关闭所有数据源");
        Collection<Map<String, Source>> values = this.sources.values();
        for (Map<String, Source> value : values) {
            Collection<Source> collection = value.values();
            for (Source source : collection) {
                IoUtil.close(source);
            }
        }
        log.info("数据源已全部关闭");
    }

}
