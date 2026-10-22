package com.woha.control.system.service;

import java.util.List;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalManagerRolePermissionRequest;
import com.woha.control.system.domain.external.ExternalManagerSaveRequest;

/**
 * 外管账号服务
 *
 * @author luowenshen
 */
public interface IExternalManagerAccountService
{
    /** 分页查询外管账号 */
    List<ExternalManagerAccount> selectAccountList(String loginName, Long wohaUid, String roleCode, Integer regionId,
            String status, String parentLoginName, String openMethod);

    /** 查询外管账号详情 */
    ExternalManagerAccount selectAccountById(Long id);

    /** 新增外管账号 */
    void createAccount(ExternalManagerSaveRequest request, String operator, String openMethod);

    /** 修改外管账号 */
    void updateAccount(Long id, ExternalManagerSaveRequest request, String operator);

    /** 修改外管账号状态 */
    void updateAccountStatus(Long id, String status);

    /** 删除外管账号 */
    void deleteAccount(Long id);

    /** 重置外管账号密码 */
    void resetPassword(Long id, String password);

    /** 校验外管账号登录 */
    ExternalManagerAccount login(String loginName, String password, String loginIp);

    /** 修改当前外管账号密码 */
    void changePassword(Long id, String oldPassword, String newPassword);

    /** 查询角色默认权限 */
    ExternalManagerRolePermissionRequest selectRolePermissions(Integer regionId);

    /** 保存角色默认权限 */
    void saveRolePermissions(ExternalManagerRolePermissionRequest request);
}
