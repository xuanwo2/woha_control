-- 外管账号及角色默认权限表
CREATE TABLE `external_manager_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `login_name` VARCHAR(20) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码哈希值',
    `real_name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `woha_uid` BIGINT NOT NULL COMMENT '关联 Woha 用户 UID',
    `role_code` VARCHAR(20) NOT NULL COMMENT '角色编码',
    `parent_id` BIGINT DEFAULT NULL COMMENT '上级外管账号主键',
    `region_id` INT NOT NULL COMMENT '所属大区编号',
    `permissions` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '功能权限编码，逗号分隔',
    `open_method` VARCHAR(50) NOT NULL DEFAULT '运营开通' COMMENT '开通方式',
    `whatsapp` VARCHAR(32) NOT NULL DEFAULT '' COMMENT 'WhatsApp账号',
    `remark` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '说明',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '账号状态，0正常，1停用',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志，0存在，2删除',
    `login_fail_count` INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    `login_fail_time` DATETIME DEFAULT NULL COMMENT '最近登录失败时间',
    `lock_expire_time` DATETIME DEFAULT NULL COMMENT '锁定截止时间',
    `login_ip` VARCHAR(128) DEFAULT '' COMMENT '最后登录 IP 地址',
    `login_date` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `pwd_update_date` DATETIME DEFAULT NULL COMMENT '密码最后修改时间',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_external_manager_account_login_name_del_flag` (`login_name`, `del_flag`),
    UNIQUE KEY `uk_external_manager_account_woha_uid_del_flag` (`woha_uid`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外管账号';

CREATE TABLE `external_manager_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `region_id` INT NOT NULL COMMENT '大区编号',
    `role_code` VARCHAR(20) NOT NULL COMMENT '角色编码',
    `permission_code` VARCHAR(50) NOT NULL COMMENT '功能权限编码',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_external_manager_role_permission` (`region_id`, `role_code`, `permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外管角色默认权限';
