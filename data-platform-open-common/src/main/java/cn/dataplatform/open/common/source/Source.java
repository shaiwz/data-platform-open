package cn.dataplatform.open.common.source;

import cn.dataplatform.open.common.annotation.Mask;
import cn.dataplatform.open.common.enums.DataSourceType;
import cn.dataplatform.open.common.enums.MaskType;
import cn.dataplatform.open.common.vo.model.KeyValue;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.Getter;

import java.io.Closeable;
import java.util.List;
import java.util.Properties;

/**
 * 〈Source〉
 *
 * @author dqw
 * @since 1.0.0
 */
@Data
public abstract class Source implements Closeable {

    protected String code;
    protected String name;

    /**
     * 连接信息
     */
    @Getter
    protected String url;
    /**
     * 可为空
     */
    @jakarta.annotation.Nullable
    protected String username;
    @Mask(type = MaskType.PASSWORD)
    @Nullable
    protected String password;

    protected Boolean isEnableHealth;

    protected Properties properties;

    /**
     * 编码
     *
     * @return 编码
     */
    public String code() {
        return this.code;
    }

    /**
     * 名称
     *
     * @return 名称
     */
    public String name() {
        return this.name;
    }

    /**
     * 数据源类型
     *
     * @return 类型
     */
    public abstract DataSourceType type();

    /**
     * 是否启用健康检查
     *
     * @return true启用
     */
    public Boolean isEnableHealth() {
        return this.isEnableHealth;
    }

    /**
     * 健康检查，失败抛出异常
     *
     * @throws Exception 检查失败原因
     */
    public abstract void health() throws Exception;

    /**
     * 初始化数据源
     */
    public void init() {
    }

    /**
     * 设置连接属性
     *
     * @param properties 连接属性
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * 设置连接属性
     *
     * @param properties 连接属性
     */
    public void setProperties(List<KeyValue> properties) {
        if (properties != null) {
            // 不支持null值
            Properties props = new Properties(properties.size());
            for (KeyValue kv : properties) {
                String key = kv.getKey();
                String value = kv.getValue();
                if (key == null || value == null) {
                    continue;
                }
                props.setProperty(key, value);
            }
            this.properties = props;
        } else {
            this.properties = new Properties();
        }
    }

    /**
     * 获取连接属性
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 属性值
     */
    public int getIntProperty(String key, int defaultValue) {
        if (this.properties != null) {
            String value = this.properties.getProperty(key);
            if (value != null) {
                return Integer.parseInt(value);
            }
        }
        return defaultValue;
    }

    /**
     * 获取连接属性
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 属性值
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        if (this.properties != null) {
            String value = this.properties.getProperty(key);
            if (value != null) {
                return Boolean.parseBoolean(value);
            }
        }
        return defaultValue;
    }

    /**
     * 获取连接属性
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 属性值
     */
    public String getStringProperty(String key, String defaultValue) {
        if (this.properties != null) {
            String value = this.properties.getProperty(key);
            if (value != null) {
                return value;
            }
        }
        return defaultValue;
    }

}