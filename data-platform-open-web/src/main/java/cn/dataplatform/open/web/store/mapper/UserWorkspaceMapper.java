package cn.dataplatform.open.web.store.mapper;

import cn.dataplatform.open.common.vo.base.PageBase;
import cn.dataplatform.open.web.store.entity.UserWorkspace;
import cn.dataplatform.open.web.store.entity.Workspace;
import cn.dataplatform.open.web.vo.user.UserData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author DaoDao
 */
public interface UserWorkspaceMapper extends BaseMapper<UserWorkspace> {

    /**
     * 根据用户查询所属的启用中的工作空间列表
     *
     * @param userId 用户id
     * @return 工作空间列表
     */
    @Select(
            "SELECT DISTINCT `workspace`.* FROM `workspace` " +
                    "JOIN `user_workspace` ON `workspace`.`id` = `user_workspace`.`workspace_id` " +
                    "WHERE `workspace`.`deleted` = FALSE " +
                    "AND `workspace`.`status` = 'ENABLE' " +
                    "AND `user_workspace`.`deleted` = FALSE " +
                    "AND `user_workspace`.`user_id` = #{userId}"
    )
    List<Workspace> listWorkspaceByUserId(Long userId);

    /**
     * 统计用户拥有权限的工作空间数量
     *
     * @param userId 用户id
     * @return 工作空间数量
     */
    @Select(
            "SELECT count(*) FROM `workspace` " +
                    "JOIN `user_workspace` ON `workspace`.`id` = `user_workspace`.`workspace_id` " +
                    "WHERE `workspace`.`deleted` = FALSE " +
                    "AND `workspace`.`status` = 'ENABLE' " +
                    "AND `user_workspace`.`deleted` = FALSE " +
                    "AND `user_workspace`.`user_id` = #{userId}"
    )
    Integer withPermission(Long userId);

    /**
     * 统计所有人员
     *
     * @param workspaceId 空间id
     * @param username    用户名称  模糊查询
     * @return t
     */
    @Select("""
            <script>
                select count(*)
                from user_workspace rew,
                user ru
                where rew.user_id = ru.id
                and rew.deleted=0 and ru.deleted=0
                and rew.workspace_id = #{workspaceId}
                and rew.is_admin = #{type}
                <if test="username != null and username != ''">
                    and  ru.username like concat('%', #{username}, '%')
                </if>
                <if test="neUser != null">
                    and rew.user_id != #{neUser}
                </if>
            </script>
            """)
    Long totalMember(@Param("workspaceId") Long workspaceId, @Param("username") String username,
                     @Param("neUser") Long neUser,
                     @Param("type") Integer type);


    /**
     * 所有人员信息
     *
     * @param workspaceId 空间id
     * @param username    用户名称  模糊查询
     * @param page        p
     * @return r
     */
    @Select("""
            <script>
                select ru.id,ru.username,ru.avatar,ru.email
                        from user_workspace rew,
                        user ru
                        where rew.user_id = ru.id
                        and rew.deleted=0 and ru.deleted=0
                        and rew.workspace_id = #{workspaceId}
                        and rew.is_admin= #{type}
                        <if test="username != null and username != ''">
                            and ru.username like concat('%', #{username}, '%')
                        </if>
                        <if test="neUser != null">
                            and rew.user_id != #{neUser}
                        </if>
                        <bind name="offset" value="(page.current-1) * page.size"></bind>
                        limit #{offset},#{page.size}
            </script>
            """)
    List<UserData> listMember(@Param("workspaceId") Long workspaceId, @Param("username") String username,
                              @Param("neUser") Long neUser,
                              @Param("type") Integer type,
                              @Param("page") PageBase page);

}
