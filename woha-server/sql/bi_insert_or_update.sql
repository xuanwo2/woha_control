-- 平台大盘菜单与权限初始化脚本
-- 执行前请确认菜单编号未被当前环境占用
INSERT INTO sys_menu VALUES('2000', '看板', '0', '5', 'bi', NULL, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', SYSDATE(), '', NULL, 'BI 看板目录');
INSERT INTO sys_menu VALUES('2001', '平台大盘', '2000', '1', 'platform', 'bi/platform/index', '', '', 1, 0, 'C', '0', '0', 'bi:platform:list', 'dashboard', 'admin', SYSDATE(), '', NULL, '平台大盘菜单');
INSERT INTO sys_role_menu(role_id, menu_id) VALUES(1, 2000);
INSERT INTO sys_role_menu(role_id, menu_id) VALUES(1, 2001);


-- 用户增长看板菜单与权限初始化脚本，在 BI 后台主库执行
-- 菜单编号 2000 为现有“看板”目录，2002 为“用户增长”菜单
INSERT INTO sys_menu
SELECT '2002', '用户增长', '2000', '2', 'userGrowth', 'bi/userGrowth/index', '', '', 1, 0,
       'C', '0', '0', 'bi:userGrowth:list', 'peoples', 'admin', SYSDATE(), '', NULL, '用户增长看板菜单'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2002);

-- 角色编号 1 为默认超级管理员角色
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 1, 2002
    WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2002);


-- 邀请增长看板菜单与权限初始化脚本，在 BI 后台主库执行
-- 菜单编号 2003 为“邀请增长”，按钮编号 2004 为质量分层导出权限
INSERT INTO sys_menu
SELECT '2003', '邀请增长', '2000', '3', 'inviteGrowth', 'bi/inviteGrowth/index', '', '', 1, 0,
       'C', '0', '0', 'bi:inviteGrowth:list', 'link', 'admin', SYSDATE(), '', NULL, '邀请增长看板菜单'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2003);

INSERT INTO sys_menu
SELECT '2004', '邀请增长导出', '2003', '1', '', '', '', '', 1, 0,
       'F', '0', '0', 'bi:inviteGrowth:export', '#', 'admin', SYSDATE(), '', NULL, '邀请人质量分层导出权限'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2004);

-- 角色编号 1 为默认超级管理员角色
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 1, 2003
    WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2003);

INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 1, 2004
    WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2004);


-- 营收看板菜单与权限初始化脚本，在 BI 后台主库执行
-- 菜单编号 2005 为“营收看板”，父级编号 2000 为“看板”目录
INSERT INTO sys_menu
SELECT '2005', '营收看板', '2000', '4', 'revenue', 'bi/revenue/index', '', '', 1, 0,
       'C', '0', '0', 'bi:revenue:list', 'money', 'admin', SYSDATE(), '', NULL, '营收看板菜单'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2005);

-- 角色编号 1 为默认超级管理员角色
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 1, 2005
    WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2005);


-- 玩法看板菜单与权限初始化脚本，在 BI 后台主库执行
-- 菜单编号 2006 为“玩法看板”，按钮编号 2007 为合规导出权限
INSERT INTO sys_menu
SELECT '2006', '玩法看板', '2000', '5', 'play', 'bi/play/index', '', '', 1, 0,
       'C', '0', '0', 'bi:play:list', 'chart', 'admin', SYSDATE(), '', NULL, '玩法看板菜单'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2006);

INSERT INTO sys_menu
SELECT '2007', '玩法合规导出', '2006', '1', '', '', '', '', 1, 0,
       'F', '0', '0', 'bi:play:export', '#', 'admin', SYSDATE(), '', NULL, '玩法合规对账导出权限'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2007);

-- 角色编号 1 为默认超级管理员角色
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 1, 2006
    WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2006);

INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 1, 2007
    WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2007);


-- 初始事件为《Woha 埋点规划 v1.2》中的核心示例事件，后续全量字典可按同一结构导入
INSERT IGNORE INTO bi_tracking_event
    (module_name, priority, event_label, event_name, report_terminal, trigger_timing,
     metric_codes, version_no, status, create_by, create_time, remark)
VALUES
    ('账号', 'P0', '注册页曝光', 'account_register_page_exposure', 'CLIENT', '注册页首次展示', 'U8', '1.2', '0', 'admin', SYSDATE(), '注册转化漏斗首阶段，客户端按页面展示上报'),
    ('账号', 'P0', '提交注册', 'account_register_submit', 'CLIENT', '用户点击提交注册且客户端校验通过', 'U8', '1.2', '0', 'admin', SYSDATE(), '注册转化漏斗提交阶段'),
    ('账号', 'P0', '注册成功', 'account_register_success', 'BOTH', '注册成功返回', 'U1 U8', '1.2', '0', 'admin', SYSDATE(), '账号注册核心事件'),
    ('启动', 'P0', '应用启动', 'app_launch', 'CLIENT', '冷启动或热启动', 'U2 U3 U4 U5 U6 U7', '1.2', '0', 'admin', SYSDATE(), '应用生命周期事件'),
    ('充值', 'P0', '充值成功', 'recharge_success', 'SERVER', '支付回调成功', 'R1 R2 R3 R4 R5 R6 R7 R8', '1.2', '0', 'admin', SYSDATE(), '服务端支付结果为准'),
    ('礼物', 'P0', '送礼成功', 'gift_send_success', 'SERVER', '送礼结算成功', 'R11 R12 R13', '1.2', '0', 'admin', SYSDATE(), '礼物结算核心事件'),
    ('玩法', 'P0', '投注成功', 'game_bet', 'SERVER', '投注成功', 'P11 P14', '1.2', '0', 'admin', SYSDATE(), '使用 game_type 区分玩法'),
    ('玩法', 'P0', '结算完成', 'game_settle', 'SERVER', '结算完成', 'P12 P13', '1.2', '0', 'admin', SYSDATE(), '玩法结算核心事件'),
    ('邀请', 'P0', '邀请绑定成功', 'invite_bind_success', 'SERVER', '邀请绑定成功', 'U13 U14 U15 U16', '1.2', '0', 'admin', SYSDATE(), '邀请关系建立事件'),
    ('房间', 'P1', '进出房', 'room_enter', 'CLIENT', '用户进入房间', 'P3', '1.2', '0', 'admin', SYSDATE(), '进入房间事件'),
    ('旧事件', '--', '房间列表曝光', 'room_list_view', 'CLIENT', '历史列表曝光', NULL, '1.1', '2', 'admin', SYSDATE(), '历史违规事件，仅保留兼容，不再上报');

-- game_bet 的 game_type 为业务私有参数
INSERT IGNORE INTO bi_tracking_event_param
    (event_id, param_name, param_type, required_flag, common_flag, description, sort_order)
SELECT event_id, 'game_type', 'string', '1', '0', '玩法类型标识', 1
FROM bi_tracking_event WHERE event_name = 'game_bet';

-- app_launch 的 launch_type 用于区分冷启动与热启动
INSERT IGNORE INTO bi_tracking_event_param
    (event_id, param_name, param_type, required_flag, common_flag, description, sort_order)
SELECT event_id, 'launch_type', 'string', '1', '0', '启动类型，取值为 cold 或 hot', 1
FROM bi_tracking_event WHERE event_name = 'app_launch';

-- v1.2 新增的八个公共参数，由 SDK 统一注入，业务事件不得重复携带同名私有参数
INSERT IGNORE INTO bi_tracking_common_param
    (param_name, param_type, description, version_no, status, create_by, create_time)
VALUES
    ('room_id', 'long', '当前房间编号，无房间上下文时为空', '1.2', '0', 'admin', SYSDATE()),
    ('room_type', 'string', '当前房间类型', '1.2', '0', 'admin', SYSDATE()),
    ('family_id', 'long', '用户所属家族编号，未加入家族时为空', '1.2', '0', 'admin', SYSDATE()),
    ('country', 'string', '用户国家或地区编码', '1.2', '0', 'admin', SYSDATE()),
    ('currency', 'string', '业务金额使用的币种编码', '1.2', '0', 'admin', SYSDATE()),
    ('scene', 'string', '事件发生的业务场景', '1.2', '0', 'admin', SYSDATE()),
    ('ab_group', 'string', '用户命中的实验分组', '1.2', '0', 'admin', SYSDATE()),
    ('attribution_channel', 'string', '用户归因渠道', '1.2', '0', 'admin', SYSDATE());

-- 菜单编号 2010 为“埋点字典”，2011 至 2014 分别为登记、修改、审批、公共参数权限
INSERT INTO sys_menu
SELECT '2010', '埋点字典', '2000', '6', 'trackingDictionary', 'bi/trackingDictionary/index', '', '',
       1, 0, 'C', '0', '0', 'bi:tracking:list', 'documentation', 'admin', SYSDATE(), '', NULL, '埋点字典平台菜单'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2010);

INSERT INTO sys_menu
SELECT '2011', '埋点事件登记', '2010', '1', '', '', '', '', 1, 0,
       'F', '0', '0', 'bi:tracking:add', '#', 'admin', SYSDATE(), '', NULL, '埋点事件登记权限'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2011);

INSERT INTO sys_menu
SELECT '2012', '埋点事件修改', '2010', '2', '', '', '', '', 1, 0,
       'F', '0', '0', 'bi:tracking:edit', '#', 'admin', SYSDATE(), '', NULL, '埋点事件修改提审与状态调整权限'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2012);

INSERT INTO sys_menu
SELECT '2013', '埋点变更审批', '2010', '3', '', '', '', '', 1, 0,
       'F', '0', '0', 'bi:tracking:review', '#', 'admin', SYSDATE(), '', NULL, '埋点事件变更审批权限'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2013);

INSERT INTO sys_menu
SELECT '2014', '埋点公共参数', '2010', '4', '', '', '', '', 1, 0,
       'F', '0', '0', 'bi:tracking:param', '#', 'admin', SYSDATE(), '', NULL, '埋点公共参数维护权限'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2014);

-- 角色编号 1 为默认超级管理员角色
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE menu_id BETWEEN 2010 AND 2014
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu role_menu
    WHERE role_menu.role_id = 1 AND role_menu.menu_id = sys_menu.menu_id
);


-- 菜单编号 2015 为“埋点质量”，2016 至 2017 分别为工单处理与质量检测刷新权限
INSERT INTO sys_menu
SELECT '2015', '埋点质量', '2000', '7', 'trackingQuality', 'bi/trackingQuality/index', '', '',
       1, 0, 'C', '0', '0', 'bi:trackingQuality:list', 'monitor', 'admin', SYSDATE(), '', NULL, '埋点质量监控菜单'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2015);

INSERT INTO sys_menu
SELECT '2016', '质量工单处理', '2015', '1', '', '', '', '', 1, 0,
       'F', '0', '0', 'bi:trackingQuality:manage', '#', 'admin', SYSDATE(), '', NULL, '埋点质量工单处理权限'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2016);

INSERT INTO sys_menu
SELECT '2017', '质量检测刷新', '2015', '2', '', '', '', '', 1, 0,
       'F', '0', '0', 'bi:trackingQuality:refresh', '#', 'admin', SYSDATE(), '', NULL, '埋点质量检测刷新权限'
    WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2017);

-- 角色编号 1 为默认超级管理员角色
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE menu_id BETWEEN 2015 AND 2017
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu role_menu
    WHERE role_menu.role_id = 1 AND role_menu.menu_id = sys_menu.menu_id
);
