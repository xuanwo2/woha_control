-- 外管礼包配置、额度及发放记录
CREATE TABLE `external_gift_pack` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '礼包主键',
    `gift_name` VARCHAR(64) NOT NULL COMMENT '礼包名称',
    `gold_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '赠送金币数量',
    `receive_limit` INT NOT NULL DEFAULT 1 COMMENT '单用户可领取次数',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_external_gift_pack_name` (`gift_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外管礼包';

CREATE TABLE `external_gift_pack_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '礼包内容主键',
    `gift_id` BIGINT NOT NULL COMMENT '礼包主键',
    `item_type` VARCHAR(16) NOT NULL COMMENT '内容类型：PROP装扮，VIP会员卡',
    `prop_id` BIGINT DEFAULT NULL COMMENT '装扮道具主键',
    `vip_level` INT DEFAULT NULL COMMENT 'VIP等级：1000、2000、3000',
    `day_num` INT NOT NULL COMMENT '有效天数',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '发放数量',
    `sort_no` INT NOT NULL DEFAULT 0 COMMENT '展示顺序',
    PRIMARY KEY (`id`),
    KEY `idx_external_gift_pack_item_gift_id` (`gift_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外管礼包内容';

CREATE TABLE `external_gift_pack_quota` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '额度主键',
    `account_id` BIGINT NOT NULL COMMENT '外管账号主键',
    `gift_id` BIGINT NOT NULL COMMENT '礼包主键',
    `quota_count` INT NOT NULL DEFAULT 0 COMMENT '可发放次数',
    `used_count` INT NOT NULL DEFAULT 0 COMMENT '已发放次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_external_gift_pack_quota_account_gift` (`account_id`, `gift_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外管账号礼包额度';

CREATE TABLE `external_gift_pack_issue` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '发放记录主键',
    `request_no` VARCHAR(64) NOT NULL COMMENT '幂等请求号',
    `gift_id` BIGINT NOT NULL COMMENT '礼包主键',
    `account_id` BIGINT NOT NULL COMMENT '外管账号主键',
    `receiver_uid` BIGINT NOT NULL COMMENT '接收用户UID',
    `region_id` INT NOT NULL COMMENT '大区编号',
    `grant_no` INT NOT NULL COMMENT '该用户第几次领取该礼包',
    `content_snapshot` VARCHAR(1000) NOT NULL COMMENT '发放内容快照',
    `vip_card_ids` VARCHAR(255) DEFAULT '' COMMENT 'VIP卡主键列表',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0已发放，1失败',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_external_gift_pack_issue_request` (`request_no`),
    UNIQUE KEY `uk_external_gift_pack_issue_receiver_grant` (`gift_id`, `receiver_uid`, `grant_no`),
    KEY `idx_external_gift_pack_issue_account_time` (`account_id`, `create_time`),
    KEY `idx_external_gift_pack_issue_receiver_time` (`receiver_uid`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外管礼包发放记录';

INSERT IGNORE INTO sys_menu VALUES
('2003', '外管礼包', '0', '7', 'external-gift-pack', 'external/GiftPack', '', '', 1, 0, 'C', '0', '0', 'external:giftPack:list', 'external-gift-pack', 'admin', sysdate(), '', null, '外管礼包一级菜单');
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2003);
