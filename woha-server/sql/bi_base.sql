-- 埋点字典平台数据表、初始字典、菜单及权限初始化脚本
-- 本脚本在 BI 后台主库执行，所有写入均设计为可重复执行

CREATE TABLE IF NOT EXISTS bi_tracking_event (
                                                 event_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '事件编号',
                                                 module_name VARCHAR(32) NOT NULL COMMENT '业务模块',
    priority VARCHAR(8) NOT NULL DEFAULT 'P2' COMMENT '事件优先级，取值为 P0、P1、P2 或 --',
    event_label VARCHAR(64) NOT NULL COMMENT '事件中文标签',
    event_name VARCHAR(128) NOT NULL COMMENT '事件名称',
    report_terminal VARCHAR(16) NOT NULL COMMENT '上报端，取值为 CLIENT、SERVER 或 BOTH',
    trigger_timing VARCHAR(255) NOT NULL COMMENT '事件触发时机',
    metric_codes VARCHAR(255) DEFAULT NULL COMMENT '引用指标编号，多个编号使用空格分隔',
    version_no VARCHAR(16) NOT NULL DEFAULT '1.2' COMMENT '字典版本',
    status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态，0 启用，1 停用，2 已废弃',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '事件补充说明',
    PRIMARY KEY (event_id),
    UNIQUE KEY uk_tracking_event_name (event_name),
    KEY idx_tracking_event_filter (module_name, report_terminal, status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='埋点事件字典';

CREATE TABLE IF NOT EXISTS bi_tracking_event_param (
                                                       param_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '参数编号',
                                                       event_id BIGINT NOT NULL COMMENT '事件编号',
                                                       param_name VARCHAR(64) NOT NULL COMMENT '参数名称',
    param_type VARCHAR(16) NOT NULL COMMENT '参数数据类型',
    required_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否必填，0 否，1 是',
    common_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否为公共参数，0 否，1 是',
    description VARCHAR(255) DEFAULT NULL COMMENT '参数说明',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    PRIMARY KEY (param_id),
    UNIQUE KEY uk_tracking_event_param (event_id, param_name),
    KEY idx_tracking_param_event (event_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='埋点事件参数';

CREATE TABLE IF NOT EXISTS bi_tracking_common_param (
                                                        common_param_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '公共参数编号',
                                                        param_name VARCHAR(64) NOT NULL COMMENT '参数名称',
    param_type VARCHAR(16) NOT NULL COMMENT '参数数据类型',
    description VARCHAR(255) NOT NULL COMMENT '参数说明',
    version_no VARCHAR(16) NOT NULL DEFAULT '1.2' COMMENT '首次启用版本',
    status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态，0 启用，1 停用，2 已废弃',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (common_param_id),
    UNIQUE KEY uk_tracking_common_param_name (param_name)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='埋点公共参数字典';

CREATE TABLE IF NOT EXISTS bi_tracking_change_record (
                                                         change_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '变更记录编号',
                                                         event_id BIGINT NOT NULL COMMENT '事件编号',
                                                         change_type VARCHAR(16) NOT NULL COMMENT '变更类型，REGISTER 登记、UPDATE 修改、STATUS 状态调整',
    change_reason VARCHAR(500) NOT NULL COMMENT '变更原因',
    snapshot_json LONGTEXT NOT NULL COMMENT '变更后的完整事件快照',
    approval_status CHAR(1) NOT NULL DEFAULT '0' COMMENT '审批状态，0 待审批，1 已通过，2 已驳回',
    create_by VARCHAR(64) DEFAULT '' COMMENT '提交人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    approve_by VARCHAR(64) DEFAULT NULL COMMENT '审批人',
    approve_time DATETIME DEFAULT NULL COMMENT '审批时间',
    approval_remark VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    PRIMARY KEY (change_id),
    KEY idx_tracking_change_event (event_id),
    KEY idx_tracking_change_status (approval_status, create_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='埋点字典变更审批记录';


-- 埋点质量监控工单、菜单及权限初始化脚本
-- 本脚本在 BI 后台主库执行，所有写入均设计为可重复执行

CREATE TABLE IF NOT EXISTS bi_tracking_quality_issue (
                                                         issue_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '质量工单编号',
                                                         region_id INT NOT NULL COMMENT '业务区域编号',
                                                         event_name VARCHAR(128) DEFAULT NULL COMMENT '关联事件名称',
    app_version VARCHAR(32) DEFAULT NULL COMMENT '关联 App 版本号',
    issue_type VARCHAR(32) NOT NULL COMMENT '问题类型，LEAK 漏报、PARAM_ERROR 参数错误、LATENCY 链路延迟、RELEASE_MISSING 版本缺失',
    severity VARCHAR(16) NOT NULL COMMENT '严重级别，HIGH 高、MEDIUM 中、LOW 低',
    title VARCHAR(128) NOT NULL COMMENT '工单标题',
    description VARCHAR(1000) NOT NULL COMMENT '问题描述',
    status CHAR(1) NOT NULL DEFAULT '0' COMMENT '处理状态，0 待处理、1 处理中、2 已解决、3 已忽略',
    assignee VARCHAR(64) DEFAULT NULL COMMENT '处理负责人',
    resolution VARCHAR(1000) DEFAULT NULL COMMENT '处理说明',
    source_key VARCHAR(255) NOT NULL COMMENT '自动检测来源唯一键',
    occurrence_count BIGINT NOT NULL DEFAULT 1 COMMENT '重复检测次数',
    first_detect_time DATETIME NOT NULL COMMENT '首次检测时间',
    last_detect_time DATETIME NOT NULL COMMENT '最近检测时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (issue_id),
    UNIQUE KEY uk_tracking_quality_source (source_key),
    KEY idx_tracking_quality_status (region_id, status, severity, last_detect_time),
    KEY idx_tracking_quality_event (event_name, issue_type)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='埋点质量监控工单';

