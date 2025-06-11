SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS `data_platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

USE `data_platform`;

create table alarm_log
(
    id             bigint auto_increment
        primary key,
    request_id     varchar(50)   null,
    robot_code     varchar(50)   not null comment '机器人编码',
    scene_code     varchar(50)   null,
    status         varchar(50)   not null,
    template_code  varchar(50)   not null comment '模板编码',
    error_reason   varchar(2000) null,
    server_name    varchar(50)   not null comment '应用名',
    instance_id    varchar(50)   not null comment '告警发出服务IP：端口',
    workspace_code varchar(50)   not null comment '数据所在工作空间',
    parameter      text          null,
    create_time    datetime      null,
    update_time    datetime      null,
    deleted        tinyint       null comment '0未删除 1已删除'
)
    comment '告警日志';

create index alarm_log_create_time_index
    on alarm_log (create_time);

create index request_id_idx
    on alarm_log (request_id);

create table alarm_robot
(
    id                bigint auto_increment
        primary key,
    code              varchar(50)  not null,
    name              varchar(50)  not null,
    type              varchar(20)  not null,
    mode              varchar(20)  not null,
    receives          json         not null,
    silent            json         not null,
    status            varchar(10)  not null,
    record_log_switch varchar(10)  not null,
    workspace_code    varchar(50)  not null,
    description       varchar(500) null,
    create_user_id    bigint       null,
    create_time       datetime     null,
    update_time       datetime     null,
    deleted           tinyint      null comment '0未删除 1已删除'
)
    comment '告警机器人';

create index alarm_robot_workspace_code_code_index
    on alarm_robot (workspace_code, code);

create table alarm_scene
(
    id             bigint auto_increment
        primary key,
    code           varchar(50)  null,
    name           varchar(100) null,
    server_name    varchar(200) null comment '服务名称',
    scene          varchar(200) null comment '场景',
    robot_code     varchar(50)  null comment '机器人编码',
    template_code  varchar(50)  null comment '模板编码',
    status         varchar(50)  null comment '启用状态',
    description    varchar(300) null comment '描述',
    workspace_code varchar(50)  null comment '数据所在工作空间',
    create_user_id bigint       null,
    create_time    datetime     null,
    update_time    datetime     null,
    deleted        tinyint      null comment '0未删除 1已删除'
)
    comment '告警场景';

create index wc_sn_s_idx
    on alarm_scene (workspace_code, server_name, scene);

create table alarm_template
(
    id                     bigint auto_increment
        primary key,
    name                   varchar(50)  null,
    code                   varchar(50)  null comment '编码',
    mode                   varchar(20)  null,
    type                   varchar(50)  null comment '模板类型，短信、邮箱、飞书、钉钉、企业微信',
    external_template_code varchar(100) null comment '外部系统消息模板编码',
    status                 varchar(10)  null,
    template_content       text         null,
    description            varchar(300) null,
    workspace_code         varchar(50)  null comment '数据所在工作空间',
    create_user_id         bigint       null,
    create_time            datetime     null,
    update_time            datetime     null,
    deleted                tinyint      null comment '0未删除 1已删除'
)
    comment '告警模板';

create index wc_c_idx
    on alarm_template (workspace_code, code);

create table data_align
(
    id                     bigint auto_increment
        primary key,
    code                   varchar(50)  null,
    name                   varchar(50)  null,
    strategy               varchar(20)  null,
    random_number          int          null comment '策略为随机模式，随机最大条数',
    trigger_type           varchar(20)  null,
    cron                   varchar(50)  null,
    block_strategy         varchar(50)  null,
    from_info              json         null,
    target_info            json         null,
    field_mapping          json         null,
    range_start            json         null,
    range_end              json         null,
    status                 varchar(10)  null,
    diff_max_record_number int          null,
    workspace_code         varchar(50)  null,
    description            varchar(500) null,
    create_user_id         bigint       null,
    create_time            datetime     null,
    update_time            datetime     null,
    deleted                tinyint      null
)
    comment '数据对齐';

create index wc_code_idx
    on data_align (workspace_code, code);

create table data_align_log
(
    id                         bigint auto_increment
        primary key,
    request_id                 varchar(50)   not null,
    workspace_code             varchar(50)   not null comment '数据所在工作空间',
    align_code                 varchar(50)   not null comment '机器人编码',
    status                     varchar(50)   not null,
    error_reason               varchar(2000) null,
    instance_id                varchar(50)   not null comment '对齐运行服务IP：端口',
    from_count                 bigint        null,
    target_count               bigint        null,
    count_cost                 bigint        null,
    diff_max_record_number     bigint        null comment '当前比对时，差异最大记录数量',
    need_compare_content_count bigint        null comment '需要对比内容数量',
    compared_content_count     bigint        null comment '已对比内容数量',
    content_cost               bigint        null,
    create_time                datetime      null,
    update_time                datetime      null
)
    comment '数据对齐日志';

create index create_time_index
    on data_align_log (create_time);

create index request_id_idx
    on data_align_log (request_id);

create table data_align_log_detail
(
    id                bigint auto_increment
        primary key,
    align_code        varchar(100) null,
    data_align_log_id bigint       not null,
    primary_key       varchar(200) null,
    from_row          text         null,
    target_row        text         null,
    diff_field_values text         null comment '差异属性值，json数组格式',
    create_time       datetime     null
)
    comment '数据对齐日志';

create index create_time_index
    on data_align_log_detail (create_time);

create index data_align_log_id
    on data_align_log_detail (data_align_log_id);

create table data_flow
(
    id                bigint auto_increment
        primary key,
    code              varchar(50)                           not null,
    name              varchar(200)                          not null,
    workspace_code    varchar(50)                           not null,
    status            varchar(10) default 'ENABLE'          not null,
    description       varchar(500)                          null,
    icon              varchar(100)                          null,
    current_version   varchar(20)                           null,
    design            text                                  null comment '数据流信息',
    datasource_codes  json                                  null,
    publish_version   varchar(20)                           null,
    enable_alarm      varchar(10) default 'ENABLE'          null,
    enable_monitor    varchar(10) default 'ENABLE'          null,
    run_strategy      varchar(50) default 'ALL_INSTANCES'   null comment '运行策略',
    instance_number   int                                   null comment '运行实例数量',
    specify_instances json                                  null comment '指定实例',
    create_user_id    bigint                                not null,
    create_time       datetime    default CURRENT_TIMESTAMP not null,
    update_time       datetime    default CURRENT_TIMESTAMP not null,
    deleted           tinyint     default 0                 not null
)
    comment '数据流';

create index wc_c__idx
    on data_flow (workspace_code, code);

create table data_flow_publish
(
    id                  bigint auto_increment
        primary key,
    code                varchar(50)                           not null,
    name                varchar(200)                          not null,
    workspace_code      varchar(50)                           not null,
    status              varchar(10) default 'ENABLE'          not null,
    publish_description varchar(2000)                         null,
    description         varchar(500)                          null,
    icon                varchar(100)                          null,
    design              text                                  null,
    datasource_codes    json                                  null,
    version             varchar(10)                           not null,
    enable_alarm        varchar(10)                           null,
    enable_monitor      varchar(10)                           null,
    run_strategy        varchar(50)                           null comment '运行策略',
    instance_number     int                                   null comment '运行实例数量',
    specify_instances   json                                  null comment '指定实例',
    create_user_id      bigint                                not null,
    create_time         datetime    default CURRENT_TIMESTAMP not null,
    update_time         datetime    default CURRENT_TIMESTAMP not null,
    deleted             tinyint     default 0                 not null
)
    comment '数据流-已发布';

create index wc_c__idx
    on data_flow_publish (workspace_code, code);

create table data_permission
(
    id                bigint auto_increment
        primary key,
    user_id           bigint      null,
    record_type       varchar(20) not null comment '数据流  数据源  数据对齐',
    record_id         bigint      not null comment '类型对应的数据ID',
    write_authority   varchar(10) null comment '有写权限',
    publish_authority varchar(10) null comment '发布权限',
    create_user_id    int         null comment '创建人',
    create_time       timestamp   null,
    update_time       timestamp   null,
    deleted           tinyint     null
)
    comment '数据权限表';

create index rt_ri_idx
    on data_permission (record_type, record_id);

create index user_idx
    on data_permission (user_id);

create table data_source
(
    id                     bigint auto_increment
        primary key,
    name                   varchar(50)                  not null,
    code                   varchar(50)                  not null,
    workspace_code         varchar(50)                  not null,
    type                   varchar(50)                  not null,
    url                    varchar(500)                 not null,
    username               varchar(50)                  null,
    password               varchar(200)                 null,
    max_pool_size          int                          null,
    driver                 varchar(200)                 null,
    status                 varchar(10) default 'ENABLE' not null,
    create_user_id         bigint                       not null,
    fe_nodes               varchar(600)                 null,
    be_nodes               varchar(600)                 null,
    nodes                  varchar(600)                 null,
    partitioning_algorithm text                         null comment '分表算法等',
    health_check           varchar(50)                  null comment '是否启用健康检查，ENABLE启用',
    mask_column            json                         null comment '敏感字段',
    description            varchar(500)                 null,
    create_time            datetime    default (now())  not null,
    update_time            datetime    default (now())  not null,
    deleted                tinyint     default 0        not null comment '0未删除 1已删除'
)
    comment '数据源';

create table data_source_health_log
(
    id               bigint auto_increment
        primary key,
    request_id       varchar(100)  null,
    data_source_code varchar(50)   not null,
    status           varchar(50)   not null,
    error_reason     varchar(2000) null,
    workspace_code   varchar(50)   not null,
    create_time      datetime      null,
    update_time      datetime      null
)
    comment '数据源健康检查记录';

create index create_time_index
    on data_source_health_log (create_time);

create index dsc_ct_idx
    on data_source_health_log (data_source_code, create_time);

create table debezium_save_point
(
    id             int auto_increment
        primary key,
    workspace_code varchar(50)  not null,
    flow_code      varchar(100) not null,
    component_code varchar(100) not null,
    instance_id    varchar(50)  not null,
    save_point     varchar(100) not null,
    `key`          varchar(300) not null,
    value          text         not null,
    create_time    timestamp    null,
    expire_time    timestamp    null
);

create index wc_fc_cc_sp_idx
    on debezium_save_point (workspace_code, flow_code, component_code, save_point);

create table debezium_schema_history
(
    id             int auto_increment
        primary key,
    workspace_code varchar(50)  not null,
    flow_code      varchar(100) not null,
    component_code varchar(100) not null,
    instance_id    varchar(50)  not null,
    schema_line    text         not null,
    create_time    timestamp    null,
    expire_time    timestamp    null
);

create index wc_fc_cc_idx
    on debezium_schema_history (workspace_code, flow_code, component_code);

create table idempotent_0
(
    id             varchar(100) not null comment '消息等ID'
        primary key,
    message_id     varchar(100) null,
    workspace_code varchar(50)  null,
    flow_code      varchar(100) null,
    component_code varchar(100) null,
    type           varchar(50)  not null comment 'Kafka、RabbitMQ、RocketMQ',
    instance_id    varchar(50)  null comment '消费实例',
    request_id     varchar(200) null comment '请求链路ID',
    create_time    timestamp    null comment '消费时间',
    expire_time    timestamp    null comment '过期时间',
    constraint idempotent_0_id_uindex
        unique (id)
)
    comment '幂等';

create index expire_time_index
    on idempotent_0 (expire_time);

create index fc_cc_index
    on idempotent_0 (flow_code, component_code);

create table idempotent_1
(
    id             varchar(100) not null comment '消息等ID'
        primary key,
    message_id     varchar(100) null,
    workspace_code varchar(50)  null,
    flow_code      varchar(100) null,
    component_code varchar(100) null,
    type           varchar(50)  not null comment 'Kafka、RabbitMQ、RocketMQ',
    instance_id    varchar(50)  null comment '消费实例',
    request_id     varchar(200) null comment '请求链路ID',
    create_time    timestamp    null comment '消费时间',
    expire_time    timestamp    null comment '过期时间',
    constraint idempotent_0_id_uindex
        unique (id)
)
    comment '幂等';

create index expire_time_index
    on idempotent_1 (expire_time);

create index fc_cc_index
    on idempotent_1 (flow_code, component_code);

create table idempotent_2
(
    id             varchar(100) not null comment '消息等ID'
        primary key,
    message_id     varchar(100) null,
    workspace_code varchar(50)  null,
    flow_code      varchar(100) null,
    component_code varchar(100) null,
    type           varchar(50)  not null comment 'Kafka、RabbitMQ、RocketMQ',
    instance_id    varchar(50)  null comment '消费实例',
    request_id     varchar(200) null comment '请求链路ID',
    create_time    timestamp    null comment '消费时间',
    expire_time    timestamp    null comment '过期时间',
    constraint idempotent_0_id_uindex
        unique (id)
)
    comment '幂等';

create index expire_time_index
    on idempotent_2 (expire_time);

create index fc_cc_index
    on idempotent_2 (flow_code, component_code);

create table idempotent_3
(
    id             varchar(100) not null comment '消息等ID'
        primary key,
    message_id     varchar(100) null,
    workspace_code varchar(50)  null,
    flow_code      varchar(100) null,
    component_code varchar(100) null,
    type           varchar(50)  not null comment 'Kafka、RabbitMQ、RocketMQ',
    instance_id    varchar(50)  null comment '消费实例',
    request_id     varchar(200) null comment '请求链路ID',
    create_time    timestamp    null comment '消费时间',
    expire_time    timestamp    null comment '过期时间',
    constraint idempotent_0_id_uindex
        unique (id)
)
    comment '幂等';

create index expire_time_index
    on idempotent_3 (expire_time);

create index fc_cc_index
    on idempotent_3 (flow_code, component_code);

create table idempotent_4
(
    id             varchar(100) not null comment '消息等ID'
        primary key,
    message_id     varchar(100) null,
    workspace_code varchar(50)  null,
    flow_code      varchar(100) null,
    component_code varchar(100) null,
    type           varchar(50)  not null comment 'Kafka、RabbitMQ、RocketMQ',
    instance_id    varchar(50)  null comment '消费实例',
    request_id     varchar(200) null comment '请求链路ID',
    create_time    timestamp    null comment '消费时间',
    expire_time    timestamp    null comment '过期时间',
    constraint idempotent_0_id_uindex
        unique (id)
)
    comment '幂等';

create index expire_time_index
    on idempotent_4 (expire_time);

create index fc_cc_index
    on idempotent_4 (flow_code, component_code);

create table idempotent_5
(
    id             varchar(100) not null comment '消息等ID'
        primary key,
    message_id     varchar(100) null,
    workspace_code varchar(50)  null,
    flow_code      varchar(100) null,
    component_code varchar(100) null,
    type           varchar(50)  not null comment 'Kafka、RabbitMQ、RocketMQ',
    instance_id    varchar(50)  null comment '消费实例',
    request_id     varchar(200) null comment '请求链路ID',
    create_time    timestamp    null comment '消费时间',
    expire_time    timestamp    null comment '过期时间',
    constraint idempotent_0_id_uindex
        unique (id)
)
    comment '幂等';

create index expire_time_index
    on idempotent_5 (expire_time);

create index fc_cc_index
    on idempotent_5 (flow_code, component_code);

create table idempotent_6
(
    id             varchar(100) not null comment '消息等ID'
        primary key,
    message_id     varchar(100) null,
    workspace_code varchar(50)  null,
    flow_code      varchar(100) null,
    component_code varchar(100) null,
    type           varchar(50)  not null comment 'Kafka、RabbitMQ、RocketMQ',
    instance_id    varchar(50)  null comment '消费实例',
    request_id     varchar(200) null comment '请求链路ID',
    create_time    timestamp    null comment '消费时间',
    expire_time    timestamp    null comment '过期时间',
    constraint idempotent_0_id_uindex
        unique (id)
)
    comment '幂等';

create index expire_time_index
    on idempotent_6 (expire_time);

create index fc_cc_index
    on idempotent_6 (flow_code, component_code);

create table idempotent_7
(
    id             varchar(100) not null comment '消息等ID'
        primary key,
    message_id     varchar(100) null,
    workspace_code varchar(50)  null,
    flow_code      varchar(100) null,
    component_code varchar(100) null,
    type           varchar(50)  not null comment 'Kafka、RabbitMQ、RocketMQ',
    instance_id    varchar(50)  null comment '消费实例',
    request_id     varchar(200) null comment '请求链路ID',
    create_time    timestamp    null comment '消费时间',
    expire_time    timestamp    null comment '过期时间',
    constraint idempotent_0_id_uindex
        unique (id)
)
    comment '幂等';

create index expire_time_index
    on idempotent_7 (expire_time);

create index fc_cc_index
    on idempotent_7 (flow_code, component_code);

create table idempotent_8
(
    id             varchar(100) not null comment '消息等ID'
        primary key,
    message_id     varchar(100) null,
    workspace_code varchar(50)  null,
    flow_code      varchar(100) null,
    component_code varchar(100) null,
    type           varchar(50)  not null comment 'Kafka、RabbitMQ、RocketMQ',
    instance_id    varchar(50)  null comment '消费实例',
    request_id     varchar(200) null comment '请求链路ID',
    create_time    timestamp    null comment '消费时间',
    expire_time    timestamp    null comment '过期时间',
    constraint idempotent_0_id_uindex
        unique (id)
)
    comment '幂等';

create index expire_time_index
    on idempotent_8 (expire_time);

create index fc_cc_index
    on idempotent_8 (flow_code, component_code);

create table idempotent_9
(
    id             varchar(100) not null comment '消息等ID'
        primary key,
    message_id     varchar(100) null,
    workspace_code varchar(50)  null,
    flow_code      varchar(100) null,
    component_code varchar(100) null,
    type           varchar(50)  not null comment 'Kafka、RabbitMQ、RocketMQ',
    instance_id    varchar(50)  null comment '消费实例',
    request_id     varchar(200) null comment '请求链路ID',
    create_time    timestamp    null comment '消费时间',
    expire_time    timestamp    null comment '过期时间',
    constraint idempotent_0_id_uindex
        unique (id)
)
    comment '幂等';

create index expire_time_index
    on idempotent_9 (expire_time);

create index fc_cc_index
    on idempotent_9 (flow_code, component_code);

create table message
(
    id           bigint unsigned auto_increment comment '消息ID'
        primary key,
    title        varchar(100)                         not null comment '消息标题',
    content      text                                 not null comment '消息内容',
    message_type varchar(20)                          not null comment '消息类型：SYSTEM系统消息, NOTICE通知, REMIND提醒',
    scope_type   varchar(20)                          not null comment '发送范围：ALL全员, WORKSPACE工作空间, SPECIFIC特定用户',
    sender_id    bigint unsigned                      not null comment '发送者ID（关联user表）',
    is_urgent    tinyint(1) default 0                 not null comment '是否紧急：0否，1是',
    status       varchar(20)                          not null comment '状态：PUBLISHED已发布, RECALLED已撤回',
    create_time  datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted      tinyint(1) default 0                 not null comment '是否删除：0否，1是'
)
    comment '站内信信息表' collate = utf8mb4_bin;

create index idx_create_time
    on message (create_time);

create index idx_sender
    on message (sender_id);

create table message_user
(
    id          bigint unsigned auto_increment comment '关联ID'
        primary key,
    message_id  bigint unsigned                      not null comment '消息ID（关联message表）',
    user_id     bigint unsigned                      not null comment '用户ID（关联user表）',
    is_read     tinyint(1) default 0                 not null comment '是否已读：0未读，1已读',
    read_time   datetime                             null comment '阅读时间',
    create_time datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint                              null,
    constraint uk_message_user
        unique (message_id, user_id)
)
    comment '站内信与用户关联表' collate = utf8mb4_bin;

create index idx_is_read
    on message_user (is_read);

create index idx_message
    on message_user (message_id);

create index idx_user
    on message_user (user_id);

create table operation_log
(
    id             bigint auto_increment
        primary key,
    username       varchar(255)  null comment '操作人',
    user_id        bigint        null,
    workspace_code varchar(255)  null comment '工作空间编码',
    workspace_name varchar(255)  null,
    `function`     varchar(255)  null,
    action         varchar(255)  null,
    record_id      bigint        null,
    request_arg    text          null,
    response_arg   text          null,
    request_id     varchar(100)  null,
    class_name     varchar(255)  null,
    method_name    varchar(255)  null,
    exception      varchar(2000) null comment '异常',
    status         varchar(20)   null,
    cost           bigint        null comment '耗时，单位毫秒',
    create_time    datetime      null
)
    comment '操作日志';

create index ct_s_idx
    on operation_log (create_time, status);

create index request_id_idx
    on operation_log (request_id);

create index user_id_idx
    on operation_log (user_id);

create index wc_ri_idx
    on operation_log (workspace_code, record_id);

create table permission
(
    id             bigint unsigned auto_increment comment 'id'
        primary key,
    code           varchar(64)                           not null comment 'code',
    name           varchar(64)                           not null comment '名称',
    status         varchar(20) default 'ENABLE'          not null comment '状态：ENABLE 启用，DISABLE 禁用',
    create_user_id bigint unsigned                       not null comment '创建用户',
    create_time    datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        tinyint(1)  default 0                 not null comment '是否已被删除：0 否，1 是'
)
    comment '@DaoDao 权限' collate = utf8mb4_bin;

create index idx_code
    on permission (code);

create table query_log
(
    id             bigint auto_increment
        primary key,
    workspace_code varchar(100)  null,
    template_code  varchar(100)  null,
    template_name  varchar(255)  null,
    request_arg    varchar(5000) null,
    response_arg   varchar(5000) null,
    request_id     varchar(255)  null,
    method         varchar(255)  null,
    status         varchar(50)   null,
    exception      varchar(2000) null,
    cost           int           null,
    number         int           null,
    hit_cache      varchar(10)   null,
    ip             varchar(100)  null,
    create_time    datetime      null
);

create index request_id_idx
    on query_log (request_id);

create index wc_tc_idx
    on query_log (workspace_code, template_code);

create table query_template
(
    id                     bigint auto_increment
        primary key,
    code                   varchar(100)                          not null,
    name                   varchar(200)                          not null,
    template               text                                  not null,
    data_source_code       varchar(100)                          not null,
    workspace_code         varchar(50)                           not null,
    timeout                int                                   null,
    enable_cache           varchar(10)                           null,
    enable_limiting        varchar(10)                           null,
    limit_rate             int                                   null,
    limit_refresh_interval int                                   null,
    limit_time_unit        varchar(20)                           null,
    record_log             varchar(10)                           null,
    secret                 varchar(64)                           not null,
    status                 varchar(10) default 'ENABLE'          not null,
    current_version        varchar(50)                           not null,
    publish_version        varchar(50)                           null,
    description            varchar(500)                          null,
    create_user_id         bigint                                not null,
    create_time            datetime    default CURRENT_TIMESTAMP not null,
    update_time            datetime    default CURRENT_TIMESTAMP not null,
    deleted                tinyint(1)  default 0                 not null
);

create table query_template_publish
(
    id                     bigint auto_increment
        primary key,
    code                   varchar(100)                         null,
    name                   varchar(200)                         null,
    template               text                                 null,
    data_source_code       varchar(100)                         null,
    workspace_code         varchar(50)                          null,
    timeout                int                                  null,
    enable_cache           varchar(10)                          null,
    enable_limiting        varchar(10)                          null,
    limit_rate             int                                  null,
    limit_refresh_interval int                                  null,
    limit_time_unit        varchar(10)                          null,
    record_log             varchar(10)                          null,
    secret                 varchar(64)                          null,
    status                 varchar(10)                          null,
    version                varchar(50)                          null,
    publish_description    varchar(500)                         null,
    description            varchar(500)                         null,
    create_user_id         bigint                               not null,
    create_time            datetime   default CURRENT_TIMESTAMP not null,
    update_time            datetime   default CURRENT_TIMESTAMP not null,
    deleted                tinyint(1) default 0                 not null
);

create index wc_code_idx
    on query_template_publish (workspace_code, code);

create table role
(
    id             bigint unsigned auto_increment comment 'id'
        primary key,
    code           varchar(64)                           not null comment 'code',
    name           varchar(64)                           not null comment '名称',
    status         varchar(20) default 'ENABLE'          not null comment '状态：ENABLE 启用，DISABLE 禁用',
    create_user_id bigint unsigned                       not null comment '创建用户',
    create_time    datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        tinyint(1)  default 0                 not null comment '是否已被删除：0 否，1 是',
    key idx_code (code),
    key idx_name (name)
)
    comment '@DaoDao 角色' collate = utf8mb4_bin;

create table role_permission
(
    id             bigint unsigned auto_increment comment 'id'
        primary key,
    role_id        bigint                               not null comment '角色 id',
    permission_id  bigint                               not null comment '权限 id',
    create_user_id bigint unsigned                      not null comment '创建用户',
    create_time    datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        tinyint(1) default 0                 not null comment '是否已被删除：0 否，1 是'
)
    comment '@DaoDao 角色权限' collate = utf8mb4_bin;

create index idx_permission_id
    on role_permission (permission_id);

create index idx_role_id
    on role_permission (role_id);

create index idx_role_id_permission_id
    on role_permission (role_id, permission_id);

create table user
(
    id             bigint unsigned auto_increment comment 'id'
        primary key,
    username       varchar(32)                           not null comment '用户名',
    gender         varchar(10)                           null,
    password       varchar(128)                          not null comment '密码',
    email          varchar(100)                          not null comment '邮箱',
    phone          varchar(20)                           null,
    avatar         varchar(255)                          null comment '头像',
    status         varchar(20) default 'ENABLE'          not null comment '状态：ENABLE 启用，DISABLE 禁用',
    description    varchar(200)                          null,
    create_user_id bigint unsigned                       not null comment '创建用户',
    create_time    datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        tinyint(1)  default 0                 not null comment '是否已被删除：0 否，1 是',
    key idx_email (email),
    key idx_username (username)
)
    comment '@DaoDao 用户' collate = utf8mb4_bin;

create table user_login_log
(
    id          bigint auto_increment
        primary key,
    request_id  varchar(100)  null,
    user_id     bigint        not null,
    username    varchar(50)   null,
    ip          varchar(50)   not null,
    browser     varchar(1000) null,
    os          varchar(200)  null,
    user_agent  varchar(2000) null,
    platform    varchar(100)  null,
    create_time datetime      null
)
    comment '登录日志';

create index request_id_idx
    on user_login_log (request_id);

create index user_id_idx
    on user_login_log (user_id);

create table user_role
(
    id             bigint unsigned auto_increment comment 'id'
        primary key,
    user_id        bigint                                not null comment '用户 id',
    role_id        bigint                                not null comment '角色 id',
    status         varchar(16) default 'ENABLE'          not null comment '状态：ENABLE 启用，DISABLE 禁用',
    create_user_id bigint unsigned                       not null comment '创建用户',
    create_time    datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        tinyint(1)  default 0                 not null comment '是否已被删除：0 否，1 是'
)
    comment '用户角色' collate = utf8mb4_bin;

create index idx_role_id
    on user_role (role_id);

create index idx_user_id
    on user_role (user_id);

create index idx_user_id_role_id
    on user_role (user_id, role_id);

create table user_workspace
(
    id             bigint unsigned auto_increment comment 'id'
        primary key,
    user_id        bigint                               not null comment '用户 id',
    workspace_id   bigint                               not null comment '工作空间ID',
    is_admin       tinyint                              null comment '1为工作空间管理员',
    create_user_id bigint unsigned                      not null comment '创建用户',
    create_time    datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        tinyint(1) default 0                 not null comment '是否已被删除：0 否，1 是'
)
    comment '用户工作空间' collate = utf8mb4_bin;

create index idx_user_id
    on user_workspace (user_id);

create index idx_workspace_id
    on user_workspace (workspace_id);

create table workspace
(
    id             bigint unsigned auto_increment comment 'id'
        primary key,
    code           varchar(64)                           not null comment 'code',
    name           varchar(64)                           not null comment '名称',
    secret         varchar(64)                           not null comment '密钥',
    status         varchar(20) default 'ENABLE'          not null comment '状态：ENABLE 启用，DISABLE 禁用',
    create_user_id bigint unsigned                       not null comment '创建用户',
    create_time    datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        tinyint(1)  default 0                 not null comment '是否已被删除：0 否，1 是',
    key idx_code (code),
    key idx_name (name)
)
    comment '工作空间' collate = utf8mb4_bin;



INSERT INTO `user` (id, username, password, email, create_user_id)
VALUES (1, 'admin', 'c649d6185032697ada52a13e7ea75bf3', 'admin@dp.test', 1);

INSERT INTO `workspace` (id, code, name, secret, create_user_id)
VALUES (1, 'test', '测试', '00000000000000000000000000000000', '1');

INSERT INTO `user_workspace` (id, user_id, workspace_id, is_admin, create_user_id)
VALUES (1, 1, 1, 1, 1);

INSERT INTO `role` (id, code, name, create_user_id)
VALUES (1, 'admin', '管理员', 1);

INSERT INTO `user_role` (id, user_id, role_id, create_user_id)
VALUES (1, 1, 1, 1);

INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (2, 'system:workspace', '工作空间', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (3, 'system:user', '用户', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (4, 'system:role', '角色', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (5, 'system:permission', '权限', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (6, 'system:permission:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (7, 'system:permission:detail', '详情', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (8, 'system:permission:add', '新增', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (9, 'system:permission:update', '修改', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (10, 'system:permission:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (11, 'system:role:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (12, 'system:role:detail', '详情', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (13, 'system:role:add', '新增', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (14, 'system:role:update', '修改', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (15, 'system:role:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (16, 'system:user:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (17, 'system:user:detail', '详情', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (18, 'system:user:add', '新增', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (19, 'system:user:update', '修改', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (20, 'system:user:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (21, 'system:workspace:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (22, 'system:workspace:user-manage', '用户管理', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (23, 'system:workspace:add', '新增', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (24, 'system:workspace:update', '修改', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (25, 'system:workspace:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (26, 'system', '系统管理', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (27, 'data', '数据管理', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (28, 'data:flow', '数据流', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (29, 'data:flow:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (30, 'data:flow:detail', '详情', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (31, 'data:flow:create', '添加', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (32, 'data:flow:update', '修改', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (33, 'data:flow:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (34, 'data:flow:publish', '发布', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (35, 'data:flow:stop', '停止', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (36, 'data:source', '数据源', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (37, 'data:source:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (38, 'data:source:detail', '详情', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (39, 'data:source:add', '添加', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (40, 'data:source:update', '修改', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (41, 'data:source:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (42, 'data:source:test', '测试连接', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (43, 'data:source:console', '控制台', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (44, 'system:user:reset-password', '重置密码', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (45, 'system:user:change-password', '修改密码', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (46, 'data:source:auth', '权限', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (47, 'data:flow:start', '运行', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (48, 'system:message', '站内信', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (49, 'system:message:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (50, 'system:message:send', '发送信息', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (51, 'system:message:recall', '撤回信息', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (52, 'system:message:delete', '删除信息', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (53, 'system:role:auth', '权限', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (54, 'system:workspace:detail', '详情', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (55, 'system:role:detail', '详情', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (56, 'security-audit:operation-log', '操作日志', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (57, 'security-audit:login-log', '登录日志', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (58, 'security-audit', '安全与审计', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (59, 'security-audit:login-log:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (60, 'security-audit:login-log:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (61, 'security-audit:operation-log:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (62, 'security-audit:operation-log:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (63, 'alarm-manage', '告警管理', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (64, 'alarm-manage:scene', '告警场景', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (65, 'alarm-manage:robot', '机器人', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (66, 'alarm-manage:template', '告警模板', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (67, 'alarm-manage:log', '告警日志', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (68, 'alarm-manage:scene:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (69, 'alarm-manage:scene:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (70, 'alarm-manage:scene:update', '修改', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (71, 'alarm-manage:scene:detail', '详情', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (72, 'alarm-manage:scene:add', '新增', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (73, 'alarm-manage:template:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (74, 'alarm-manage:template:add', '新增', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (75, 'alarm-manage:template:update', '修改', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (76, 'alarm-manage:template:detail', '详情', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (77, 'alarm-manage:template:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (78, 'alarm-manage:log:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (79, 'alarm-manage:log:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (80, 'alarm-manage:robot:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (81, 'alarm-manage:robot:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (82, 'alarm-manage:robot:update', '修改', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (83, 'alarm-manage:robot:add', '新增', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (84, 'alarm-manage:robot:detail', '详情', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (85, 'server-manage', '服务管理', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (86, 'server-manage:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (87, 'server-manage:monitor', '监控', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (88, 'query-manage', '查询管理', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (89, 'query-manage:template', '查询模板', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (90, 'query-manage:template:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (91, 'query-manage:template:add', '新增', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (92, 'query-manage:template:update', '修改', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (93, 'query-manage:template:detail', '详情', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (94, 'query-manage:template:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (95, 'query-manage:template:version', '版本', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (96, 'query-manage:template:auth', '权限', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (97, 'query-manage:query-log', '查询日志', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (98, 'query-manage:query-log:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (99, 'query-manage:query-log:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (100, 'data:align', '数据对齐', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (101, 'data:align:list', '列表', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (102, 'data:align:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (103, 'data:align:detail', '详情', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (104, 'data:align:update', '修改', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (105, 'data:align:log', '日志', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (106, 'data:align:auth', '权限', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (107, 'data:align:log:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (108, 'data:align:add', '新增', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (109, 'data:source:log', '日志', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (110, 'data:source:log:delete', '删除', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (111, 'data:flow:log', '日志', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (112, 'data:flow:auth', '权限', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (113, 'data:flow:version', '版本', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (114, 'dashboard', '仪表盘', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (115, 'dashboard:flow', '数据流统计', 1);
INSERT INTO `permission` (id, code, name, create_user_id)
VALUES (116, 'dashboard:query', '查询统计', 1);

INSERT INTO `role_permission` (role_id, permission_id, create_user_id)
SELECT 1, id, 1
FROM `permission`
WHERE id NOT IN (SELECT permission_id FROM `role_permission` WHERE role_id = 1)
