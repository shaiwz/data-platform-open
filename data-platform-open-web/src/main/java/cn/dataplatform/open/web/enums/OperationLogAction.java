package cn.dataplatform.open.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/1
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum OperationLogAction {

    /**
     * 新增
     */
    ADD("新增"),
    /**
     * 保存,暂存
     */
    SAVE("保存"),
    /**
     * 删除
     */
    DELETE("删除"),
    /**
     * 修改
     */
    UPDATE("修改"),
    /**
     * 发布
     */
    PUBLISH("发布"),
    /**
     * 停止
     */
    STOP("停止"),
    /**
     * 启动
     */
    START("启动"),
    /**
     * 回退
     */
    ROLLBACK("回退"),
    /**
     * 上传文件
     */
    UPLOAD_FILE("上传文件"),
    ;

    private final String name;

}
