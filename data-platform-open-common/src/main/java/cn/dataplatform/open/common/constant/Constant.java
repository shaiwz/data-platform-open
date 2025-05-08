package cn.dataplatform.open.common.constant;


public interface Constant {

    String REQUEST_ID = "requestId";
    /**
     * workspaceCode
     */
    String WORKSPACE_CODE = "workspaceCode";

    String TIME_ZONE = "GMT+8";
    String DATE_FORMAT = "yyyy-MM-dd";
    String TIME_FORMAT = "HH:mm:ss";
    String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 带毫秒的时间格式
     */
    String DATE_TIME_MILLIS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * oracle 时间格式
     */
    String ORACLE_DATE_TIME_FORMAT = "YYYY-MM-DD HH24:MI:SS";

    String DATE_EXAMPLE = "2025-01-01";
    String TIME_EXAMPLE = "00:00:00";
    String DATE_TIME_EXAMPLE = "2025-01-01 00:00:00";

    /**
     * limit 1
     */
    String LIMIT_ONE = "limit 1";


    /**
     * flowCode
     */
    String FLOW_CODE = "flowCode";
    /**
     * flowComponentCode
     */
    String FLOW_COMPONENT_CODE = "flowComponentCode";
    /**
     * queryMethod
     */
    String QUERY_METHOD = "queryMethod";
    /**
     * queryTemplateCode
     */
    String QUERY_TEMPLATE_CODE = "queryTemplateCode";

}