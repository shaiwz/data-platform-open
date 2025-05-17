package cn.dataplatform.open.common.source;

import cn.hutool.core.io.IoUtil;
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
    public Source getSource(String workspace, String datasourceCode) {
        Map<String, Source> sourceMap = sources.get(workspace);
        if (sourceMap == null) {
            return null;
        }
        return sourceMap.get(datasourceCode);
    }

    /**
     * 添加数据源
     *
     * @param workspace 工作空间
     * @param source    数据源
     */
    public synchronized void addSource(String workspace, Source source) {
        Source originSource = this.getSource(workspace, source.code());
        // put
        Map<String, Source> sourceMap = sources.computeIfAbsent(workspace, (key) -> new ConcurrentHashMap<>());
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
    public Source removeSource(String workspace, String datasourceCode) {
        Map<String, Source> sourceMap = sources.get(workspace);
        if (sourceMap == null) {
            return null;
        }
        return sourceMap.remove(datasourceCode);
    }

    /**
     * 获取所有数据源
     */
    public Map<String, Map<String, Source>> getAllSource() {
        return sources;
    }

    /**
     * 关闭所有数据源
     */
    @Override
    public void close() {
        log.info("开始关闭所有数据源");
        Collection<Map<String, Source>> values = sources.values();
        for (Map<String, Source> value : values) {
            Collection<Source> collection = value.values();
            for (Source source : collection) {
                IoUtil.close(source);
            }
        }
        log.info("数据源已全部关闭");
    }

}
