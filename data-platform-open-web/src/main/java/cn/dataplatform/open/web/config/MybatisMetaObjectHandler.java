package cn.dataplatform.open.web.config;

import cn.dataplatform.open.common.enums.Deleted;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2020/8/26
 * @since 1.0.0
 */
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    private static final String DELETED = "deleted";
    private static final String CREATE_TIME = "createTime";
    private static final String UPDATE_TIME = "updateTime";

    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName(DELETED, Deleted.ENABLE.getStatus(), metaObject);
        LocalDateTime now = LocalDateTime.now();
        {
            Object value = metaObject.getValue(CREATE_TIME);
            if (value == null) {
                this.setFieldValByName(CREATE_TIME, now, metaObject);
            }
        }
        Object value = metaObject.getValue(UPDATE_TIME);
        if (value == null) {
            this.setFieldValByName(UPDATE_TIME, now, metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName(UPDATE_TIME, LocalDateTime.now(), metaObject);
    }

}
