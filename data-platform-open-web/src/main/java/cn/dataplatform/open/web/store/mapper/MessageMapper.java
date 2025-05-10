package cn.dataplatform.open.web.store.mapper;


import cn.dataplatform.open.web.store.entity.Message;
import cn.dataplatform.open.web.vo.message.CountsResponse;
import cn.dataplatform.open.web.vo.message.MessageListRequest;
import cn.dataplatform.open.web.vo.message.MyMessageResponse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/3/29
 * @since 1.0.0
 */
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 我的消息
     *
     * @param page  分页
     * @param id    用户ID
     * @param query 查询条件
     * @return r
     */
    @Select("""
            <script>
            SELECT
                        m.id,
                        m.title,
                        m.content,
                        m.message_type AS messageType,
                        m.scope_type AS scopeType,
                        m.is_urgent AS isUrgent,
                        m.status,
                        m.create_time AS createTime,
                        mu.id as muId,
                        mu.is_read AS `read`,
                        mu.read_time AS readTime
                    FROM
                        message m
                    JOIN
                        message_user mu ON m.id = mu.message_id
                    WHERE
                        mu.user_id = #{userId}
                        and mu.deleted = 0
                        and m.deleted = 0
                    <if test="query.title != null and query.title != ''">
                        AND m.title LIKE CONCAT('%', #{query.title}, '%')
                    </if>
                    <if test="query.messageType != null and query.messageType != ''">
                        AND m.message_type = #{query.messageType}
                    </if>
                    <if test="query.isUrgent != null">
                        AND m.is_urgent = #{query.isUrgent}
                    </if>
                    <if test="query.status != null and query.status != ''">
                        AND m.status = #{query.status}
                    </if>
                    <if test="query.startCreateTime != null">
                        AND m.create_time &gt;= #{query.startCreateTime}
                    </if>
                    <if test="query.endCreateTime != null">
                        AND m.create_time &lt;= #{query.endCreateTime}
                    </if>
                    <if test="query.read != null">
                        AND mu.is_read = #{query.read}
                    </if>
                    ORDER BY
                        mu.is_read ASC,
                        m.is_urgent DESC,
                        m.create_time DESC
            </script>
            """)
    Page<MyMessageResponse> myMessage(Page<Object> page, @Param("userId") Long id,
                                      @Param("query") MessageListRequest query);

    /**
     * 获取total  unread
     *
     * @param userId 用户id
     * @param query  查询条件
     * @return r
     */
    @Select("""
            <script>
            SELECT
                        COUNT(1) AS total,
                        SUM(CASE WHEN mu.is_read = 0 THEN 1 ELSE 0 END) AS unread
                    FROM
                        message m
                    JOIN
                        message_user mu ON m.id = mu.message_id
                    WHERE
                        mu.user_id = #{userId}
                        and mu.deleted = 0
                        and m.deleted = 0
                    <if test="query.title != null and query.title != ''">
                        AND m.title LIKE CONCAT('%', #{query.title}, '%')
                    </if>
                    <if test="query.messageType != null and query.messageType != ''">
                        AND m.message_type = #{query.messageType}
                    </if>
                    <if test="query.isUrgent != null">
                        AND m.is_urgent = #{query.isUrgent}
                    </if>
                    <if test="query.status != null and query.status != ''">
                        AND m.status = #{query.status}
                    </if>
                    <if test="query.startCreateTime != null">
                        AND m.create_time &gt;= #{query.startCreateTime}
                    </if>
                    <if test="query.endCreateTime != null">
                        AND m.create_time &lt;= #{query.endCreateTime}
                    </if>
                    <if test="query.read != null">
                        AND mu.is_read = #{query.read}
                    </if>
            </script>
            """)
    CountsResponse counts(@Param("userId") Long userId, @Param("query") MessageListRequest query);

}
