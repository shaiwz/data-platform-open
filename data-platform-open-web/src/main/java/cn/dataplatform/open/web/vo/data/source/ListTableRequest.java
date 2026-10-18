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
package cn.dataplatform.open.web.vo.data.source;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 查询某库下的表请求（支持表名模糊、limit）
 *
 * @author dingqianwen
 * @since 1.0.0
 */
@Data
public class ListTableRequest {

    private Long id;

    private String code;

    /**
     * 库名（schema），必填，查该库下的表
     */
    @NotBlank
    private String schemaCode;

    /**
     * 表名模糊匹配，为空查全部
     */
    private String tableCode;

    /**
     * 一次最多返回表数量，小于等于0不限制
     */
    private Integer limit;

}
