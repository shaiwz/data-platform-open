package cn.dataplatform.open.web.store.mapper;


import cn.dataplatform.open.web.store.entity.Role;
import cn.dataplatform.open.web.store.entity.User;
import cn.dataplatform.open.web.store.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author DaoDao
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {

    @Select(
            "SELECT DISTINCT `role`.* FROM `role` " +
                    "JOIN `user_role` ON `role`.`id` = `user_role`.`role_id` " +
                    "WHERE `role`.`deleted` = FALSE " +
                    "AND `role`.`status` = 'ENABLE' " +
                    "AND `user_role`.`deleted` = FALSE " +
                    "AND `user_role`.`status` = 'ENABLE' " +
                    "AND `user_role`.`user_id` = #{userId}"
    )
    List<Role> listRoleByUserId(Long userId);

    @Select(
            "SELECT DISTINCT `user`.* FROM `user` " +
                    "JOIN `user_role` ON `user`.`id` = `user_role`.`user_id` " +
                    "WHERE `user`.`deleted` = FALSE " +
                    "AND `user`.`status` = 'ENABLE' " +
                    "AND `user_role`.`deleted` = FALSE " +
                    "AND `user_role`.`status` = 'ENABLE' " +
                    "AND `user_role`.`role_id` = #{roleId}"
    )
    List<User> listUserByRoleId(Long roleId);

}
