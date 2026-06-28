/*
 * ============================================================================
 *
 *                    数海文舟 (DATA PLATFORM) 版权所有 © 2025
 *
 *       本软件受著作权法和国际版权条约保护，本软件受著作权法和国际版权条约保护，未经明确书面授权，任何单位或个人不得对本软件进行复制、修改、分发、
 *       逆向工程、商业用途等任何形式的非法使用。违者将面临人民币100万元的
 *       法定罚款及可能的法律追责。
 *
 *       举报侵权行为可获得实际罚款金额40%的现金奖励。
 *       举报渠道：
 *           - 法务邮箱：dingqw@shaiwz.com，761945125@qq.com
 *
 *       COPYRIGHT (C) 2025 dingqianwen COMPANY. ALL RIGHTS RESERVED.
 *
 * ============================================================================
 */
package cn.dataplatform.open.common.component;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import org.apache.ibatis.type.JdbcType;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.Arrays;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2026/1/28
 * @since 1.0.0
 */
public class UniversalJsonTypeHandler extends Fastjson2TypeHandler {

    private final DbType dbType;


    /**
     * 获取当前环境数据库类型
     * <p>
     * 不允许删除此构造器：at org.apache.ibatis.type.TypeHandlerRegistry.getInstance(TypeHandlerRegistry.java:462)
     *
     * @param type Java类型
     * @throws SQLException SQL异常
     */
    public UniversalJsonTypeHandler(Class<?> type) throws SQLException {
        super(type);
        this.dbType = getDbType();
    }

    /**
     * 获取当前环境数据库类型
     * <p>
     * 不允许删除此构造器：com.baomidou.mybatisplus.core.toolkit.MybatisUtils#newJsonTypeHandler
     *
     * @param type  Java类型
     * @param field 字段属性
     * @throws SQLException SQL异常
     */
    public UniversalJsonTypeHandler(Class<?> type, Field field) throws SQLException {
        super(type, field);
        this.dbType = getDbType();
    }


    /**
     * 获取db类型
     *
     * @return DbType
     * @throws SQLException sql异常
     */
    private DbType getDbType() throws SQLException {
        DataSource dataSource = SpringUtil.getBean(DataSource.class);
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            return JdbcUtils.getDbType(url);
        }
    }

    /**
     * 设置非空参数
     *
     * @param ps        PreparedStatement
     * @param i         当前参数索引
     * @param parameter 参数值
     * @param jdbcType  JDBC类型
     * @throws SQLException SQL异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException {
        // 如果是pgsql等特殊处理，需要指定Types.OTHER
        if (Arrays.asList(DbType.POSTGRE_SQL, DbType.KINGBASE_ES).contains(this.dbType)) {
            // 如果已经是json字符串
            if (parameter instanceof String) {
                ps.setObject(i, parameter, Types.OTHER);
                return;
            }
            ps.setObject(i, super.toJson(parameter), Types.OTHER);
            return;
        }
        // 其他数据库走默认逻辑
        super.setNonNullParameter(ps, i, parameter, jdbcType);
    }

    /**
     * 获取可空结果
     *
     * @param rs          结果集
     * @param columnIndex 列索引
     * @return 结果对象
     * @throws SQLException SQL异常
     */
    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        final String json = rs.getString(columnIndex);
        // 如果目标是String，直接返回
        if (this.getFieldType().equals(String.class)) {
            return json;
        }
        return StringUtils.isBlank(json) ? null : this.parse(json);
    }

    /**
     * 获取可空结果
     *
     * @param rs         结果集
     * @param columnName 列名称
     * @return 结果对象
     * @throws SQLException SQL异常
     */
    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final String json = rs.getString(columnName);
        // 如果目标是String，直接返回
        if (this.getFieldType().equals(String.class)) {
            return json;
        }
        return StringUtils.isBlank(json) ? null : this.parse(json);
    }

    /**
     * 获取可空结果
     *
     * @param cs          可调用语句
     * @param columnIndex 列索引
     * @return 结果对象
     * @throws SQLException SQL异常
     */
    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        final String json = cs.getString(columnIndex);
        // 如果目标是String，直接返回
        if (this.getFieldType().equals(String.class)) {
            return json;
        }
        return StringUtils.isBlank(json) ? null : this.parse(json);
    }

}
