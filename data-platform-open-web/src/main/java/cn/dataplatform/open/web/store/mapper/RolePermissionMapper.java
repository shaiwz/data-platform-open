package cn.dataplatform.open.web.store.mapper;


import cn.dataplatform.open.web.store.entity.Permission;
import cn.dataplatform.open.web.store.entity.Role;
import cn.dataplatform.open.web.store.entity.RolePermission;
import cn.dataplatform.open.web.store.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select(
            "SELECT DISTINCT `permission`.* FROM `permission` " +
                    "JOIN `role_permission` ON `permission`.`id` = `role_permission`.`permission_id` " +
                    "JOIN `role` ON `role`.`id` = `role_permission`.`role_id` " +
                    "WHERE `permission`.`deleted` = FALSE " +
                    "AND `permission`.`status` = 'ENABLE' " +
                    "AND `role_permission`.`deleted` = FALSE " +
                    "AND `role`.`deleted` = FALSE " +
                    "AND `role`.`status` = 'ENABLE' " +
                    "AND `role_permission`.`role_id` = #{roleId}"
    )
    List<Permission> listPermissionByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID查询角色列表
     *
     * @param permissionId 权限ID
     * @return 角色列表
     */
    @Select(
            "SELECT DISTINCT `role`.* FROM `role` " +
                    "JOIN `role_permission` ON `role`.`id` = `role_permission`.`role_id` " +
                    "JOIN `permission` ON `permission`.`id` = `role_permission`.`permission_id` " +
                    "WHERE `role`.`deleted` = FALSE " +
                    "AND `role`.`status` = 'ENABLE' " +
                    "AND `role_permission`.`deleted` = FALSE " +
                    "AND `permission`.`deleted` = FALSE " +
                    "AND `permission`.`status` = 'ENABLE' " +
                    "AND `role_permission`.`permission_id` = #{permissionId}"
    )
    List<Role> listRoleByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select(
            "SELECT DISTINCT `permission`.* FROM `permission` " +
                    "JOIN `role_permission` ON `permission`.`id` = `role_permission`.`permission_id` " +
                    "JOIN `role` ON `role`.`id` = `role_permission`.`role_id` " +
                    "JOIN `user_role` ON `role`.`id` = `user_role`.`role_id` " +
                    "WHERE `permission`.`deleted` = FALSE " +
                    "AND `permission`.`status` = 'ENABLE' " +
                    "AND `role_permission`.`deleted` = FALSE " +
                    "AND `role`.`deleted` = FALSE " +
                    "AND `role`.`status` = 'ENABLE' " +
                    "AND `user_role`.`deleted` = FALSE " +
                    "AND `user_role`.`status` = 'ENABLE' " +
                    "AND `user_role`.`user_id` = #{userId}"
    )
    List<Permission> listPermissionByUserId(@Param("userId") Long userId);

    /**
     * 查询当前用户的权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("""
            <script>
            SELECT COUNT(DISTINCT `permission`.`id`) > 0 FROM `permission`
            JOIN `role_permission` ON `permission`.`id` = `role_permission`.`permission_id`
            JOIN `role` ON `role`.`id` = `role_permission`.`role_id`
            JOIN `user_role` ON `role`.`id` = `user_role`.`role_id`
            WHERE `permission`.`deleted` = FALSE
            AND `permission`.`status` = 'ENABLE'
            AND `role_permission`.`deleted` = FALSE
            AND `role`.`deleted` = FALSE
            AND `role`.`status` = 'ENABLE'
            AND `user_role`.`deleted` = FALSE
            AND `user_role`.`status` = 'ENABLE'
            AND `user_role`.`user_id` = #{userId}
            AND `permission`.`code` IN
            <foreach item='code' collection='permissionCodes' open='(' separator=',' close=')'>
                #{code}
            </foreach>
            </script>
            """)
    Boolean hasAnyPermission(@Param("userId") Long userId, @Param("permissionCodes") List<String> permissionCodes);

    /**
     * 根据权限ID查询用户列表
     *
     * @param permissionId 权限ID
     * @return 用户列表
     */
    @Select(
            "SELECT DISTINCT `user`.* FROM `user` " +
                    "JOIN `user_role` ON `user`.`id` = `user_role`.`user_id` " +
                    "JOIN `role` ON `role`.`id` = `user_role`.`role_id` " +
                    "JOIN `role_permission` ON `role`.`id` = `role_permission`.`role_id` " +
                    "JOIN `permission` ON `permission`.`id` = `role_permission`.`permission_id` " +
                    "WHERE `user`.`deleted` = FALSE " +
                    "AND `user`.`status` = 'ENABLE' " +
                    "AND `user_role`.`deleted` = FALSE " +
                    "AND `user_role`.`status` = 'ENABLE' " +
                    "AND `role`.`deleted` = FALSE " +
                    "AND `role`.`status` = 'ENABLE' " +
                    "AND `role_permission`.`deleted` = FALSE " +
                    "AND `permission`.`deleted` = FALSE " +
                    "AND `permission`.`status` = 'ENABLE' " +
                    "AND `role_permission`.`permission_id` = #{permissionId}"
    )
    List<User> listUserByPermissionId(@Param("permissionId") Long permissionId);

}
