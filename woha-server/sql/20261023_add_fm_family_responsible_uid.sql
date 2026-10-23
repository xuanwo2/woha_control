ALTER TABLE `fm_family`
    ADD COLUMN `responsible_uid` BIGINT NULL COMMENT '外管负责人Woha用户UID' AFTER `uid`,
    ADD KEY `idx_responsible_uid` (`responsible_uid`);
