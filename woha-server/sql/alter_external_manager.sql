INSERT IGNORE INTO sys_menu VALUES
('2001', '外管账号', '0', '5', 'external-account', 'external/Account', '', '', 1, 0, 'C', '0', '0', 'external:account:list', 'user', 'admin', sysdate(), '', null, '外管账号一级菜单'),
('2002', '角色默认权限', '0', '6', 'external-role-permission', 'external/RolePermission', '', '', 1, 0, 'C', '0', '0', 'external:rolePermission:list', 'lock', 'admin', sysdate(), '', null, '外管角色默认权限一级菜单');

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2001),
(1, 2002);

INSERT IGNORE INTO sys_menu VALUES
('2003', '外管礼包', '0', '7', 'external-gift-pack', 'external/GiftPack', '', '', 1, 0, 'C', '0', '0', 'external:giftPack:list', 'external-gift-pack', 'admin', sysdate(), '', null, '外管礼包一级菜单');
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2003);

INSERT IGNORE INTO sys_menu VALUES
('2005', '外管操作记录', '0', '9', 'external-operation-log', 'external/OperationLog', '', '', 1, 0, 'C', '0', '0', 'external:operationLog:list', 'form', 'admin', sysdate(), '', null, '外管操作记录菜单');
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2005);