package cn.dataplatform.open.web.store.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/1/4
 * @since 1.0.0
 */
@TableName("data_flow_publish")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DataFlowPublish implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;
    private String workspaceCode;

    /**
     * 启用,禁用
     *
     * @see Status
     */
    private String status;
    /**
     * 发布描述
     */
    private String publishDescription;
    /**
     * 描述
     */
    private String description;
    private String icon;

    /**
     * {"nodes":[{"id":"1736591264713991885","type":"job","x":360,"y":114.6171875,"properties":{"id":"30","name":"双击进行编辑","cron":"* * * * * ?","description":"","status":"ENABLE","retryType":1,"retryCount":3,"retryInterval":5000,"retryExpression":"1000,5000,10000","blockStrategy":"ABANDON_CURRENT","width":140,"height":100}}],"edges":[]}
     */
    private String design;
    /**
     * 引用的数据源编码 json数组
     */
    private String datasourceCodes;

    private String version;

    private String enableAlarm;

    private String enableMonitor;

    /**
     * 运行策略
     * <p>
     * ALL_INSTANCES 全部实例
     * SPECIFY_INSTANCES 指定实例
     * FIXED_INSTANCE_NUMBER 固定实例数
     */
    private String runStrategy;

    /**
     * 运行实例数量
     */
    private Integer instanceNumber;

    /**
     * 指定实例
     */
    private String specifyInstances;

    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

}
