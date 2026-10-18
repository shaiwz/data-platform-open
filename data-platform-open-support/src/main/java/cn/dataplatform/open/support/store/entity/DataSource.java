package cn.dataplatform.open.support.store.entity;

import cn.dataplatform.open.common.component.UniversalJsonTypeHandler;
import cn.dataplatform.open.common.vo.model.KeyValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/3
 * @since 1.0.0
 */
@TableName(value = "data_source", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DataSource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;
    private String workspaceCode;

    /**
     * MySQL Doris StarRocks Oracle等
     */
    private String dataSourceType;

    private String url;

    private String username;

    private String password;

    private String driver;

    /**
     * 启用,禁用
     */
    private String status;

    /**
     * 分表规则
     */
    private String partitioningAlgorithm;

    private String healthCheck;
    /**
     * 连接配置
     */
    @TableField(typeHandler = UniversalJsonTypeHandler.class)
    private List<KeyValue> properties;

    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;


}
