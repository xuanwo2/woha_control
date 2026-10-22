INSERT IGNORE INTO sys_menu VALUES
('2001', '外管账号', '0', '5', 'external-account', 'external/Account', '', '', 1, 0, 'C', '0', '0', 'external:account:list', 'user', 'admin', sysdate(), '', null, '外管账号一级菜单'),
('2002', '角色默认权限', '0', '6', 'external-role-permission', 'external/RolePermission', '', '', 1, 0, 'C', '0', '0', 'external:rolePermission:list', 'lock', 'admin', sysdate(), '', null, '外管角色默认权限一级菜单');

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2001),
(1, 2002);
