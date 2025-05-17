package cn.dataplatform.open.web.store.mapper;

import cn.dataplatform.open.web.store.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * @author DaoDao
 */
public interface UserMapper extends BaseMapper<User> {


    /**
     * 根据id查询所有用户列表，包含删除的 禁用的
     *
     * @param ids 用户id列表
     * @return 用户列表
     */
    @Select("""
                 <script>
                    SELECT * FROM user
                    WHERE id in
                    <foreach item="item" index="index" collection="ids" open="(" separator="," close=")">
                        #{item}
                    </foreach>
                 </script>
            """)
    List<User> listAllByIds(@Param("ids") Collection<Long> ids);


}
