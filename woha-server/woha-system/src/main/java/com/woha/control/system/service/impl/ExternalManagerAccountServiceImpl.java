package com.woha.control.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.woha.control.common.exception.ServiceException;
import com.woha.control.common.utils.StringUtils;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalManagerPermission;
import com.woha.control.system.domain.external.ExternalManagerRole;
import com.woha.control.system.domain.external.ExternalManagerRolePermission;
import com.woha.control.system.domain.external.ExternalManagerRolePermissionRequest;
import com.woha.control.system.domain.external.ExternalManagerSaveRequest;
import com.woha.control.system.domain.external.ExternalWohaUser;
import com.woha.control.system.mapper.ExternalManagerAccountMapper;
import com.woha.control.system.mapper.ExternalManagerRolePermissionMapper;
import com.woha.control.system.service.IExternalManagerAccountService;
import com.woha.control.system.service.IExternalWohaUserService;

/**
 * 外管账号服务实现
 *
 * @author luowenshen
 */
@Service
public class ExternalManagerAccountServiceImpl implements IExternalManagerAccountService
{
    private static final int LOGIN_MAX_FAILURE_COUNT = 5;
    private static final long LOGIN_LOCK_MILLIS = 7L * 24 * 60 * 60 * 1000;

    private final ExternalManagerAccountMapper accountMapper;
    private final ExternalManagerRolePermissionMapper rolePermissionMapper;
    private final IExternalWohaUserService wohaUserService;
    private final PasswordEncoder passwordEncoder;

    public ExternalManagerAccountServiceImpl(ExternalManagerAccountMapper accountMapper,
            ExternalManagerRolePermissionMapper rolePermissionMapper, IExternalWohaUserService wohaUserService,
            PasswordEncoder passwordEncoder)
    {
        this.accountMapper = accountMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.wohaUserService = wohaUserService;
        this.passwordEncoder = passwordEncoder;
    }

    /** 查询外管账号列表 */
    @Override
    public List<ExternalManagerAccount> selectAccountList(String loginName, Long wohaUid, String roleCode,
            Integer regionId, String status, String parentLoginName, String openMethod)
    {
        return accountMapper.selectAccountList(loginName, wohaUid, roleCode, regionId, status, parentLoginName, openMethod);
    }

    /** 查询外管账号详情 */
    @Override
    public ExternalManagerAccount selectAccountById(Long id)
    {
        ExternalManagerAccount account = accountMapper.selectAccountById(id);
        if (account == null)
        {
            throw new ServiceException("外管账号不存在");
        }
        return account;
    }

    /** 新增外管账号 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAccount(ExternalManagerSaveRequest request, String operator, String openMethod)
    {
        ExternalWohaUser user = validateRequest(request, null, true);
        ExternalManagerAccount account = buildAccount(request, operator, user);
        account.setOpenMethod(openMethod);
        if (request.getPermissions() == null || request.getPermissions().isEmpty())
        {
            account.setPermissions(joinPermissions(findPermissions(rolePermissionMapper.selectByRegionId(request.getRegionId()),
                    ExternalManagerRole.valueOf(request.getRoleCode()))));
        }
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        accountMapper.insertAccount(account);
    }

    /** 修改外管账号 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccount(Long id, ExternalManagerSaveRequest request, String operator)
    {
        ExternalManagerAccount existing = selectAccountById(id);
        ExternalWohaUser user = validateRequest(request, id, false);
        ExternalManagerAccount account = buildAccount(request, operator, user);
        account.setId(id);
        if (StringUtils.isNotEmpty(request.getPassword()))
        {
            validatePassword(request.getPassword());
            account.setPassword(passwordEncoder.encode(request.getPassword()));
            account.setPwdUpdateDate(new Date());
        }
        if (!existing.getLoginName().equals(request.getLoginName()))
        {
            throw new ServiceException("登录账号创建后不可修改");
        }
        account.setLoginName(existing.getLoginName());
        accountMapper.updateAccount(account);
    }

    /** 修改外管账号状态 */
    @Override
    public void updateAccountStatus(Long id, String status)
    {
        if (!"0".equals(status) && !"1".equals(status))
        {
            throw new ServiceException("账号状态不合法");
        }
        selectAccountById(id);
        accountMapper.updateAccountStatus(id, status);
    }

    /** 删除外管账号 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(Long id)
    {
        ExternalManagerAccount account = selectAccountById(id);
        if (ExternalManagerRole.SUPER_ADMIN.name().equals(account.getRoleCode())
                && accountMapper.countActiveAccountByRole(ExternalManagerRole.SUPER_ADMIN.name()) <= 1)
        {
            throw new ServiceException("至少保留一个 Super Admin 账号");
        }
        accountMapper.deleteAccount(id);
    }

    /** 重置外管账号密码 */
    @Override
    public void resetPassword(Long id, String password)
    {
        selectAccountById(id);
        validatePassword(password);
        accountMapper.resetPassword(id, passwordEncoder.encode(password));
    }

    /** 校验外管账号登录 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExternalManagerAccount login(String loginName, String password, String loginIp)
    {
        ExternalManagerAccount account = accountMapper.selectAccountByLoginNameForUpdate(loginName);
        if (account == null || !passwordEncoder.matches(password, account.getPassword()))
        {
            if (account != null)
            {
                recordLoginFailure(account);
            }
            throw new ServiceException("账号或密码错误");
        }
        if ("1".equals(account.getStatus()))
        {
            throw new ServiceException("账号已停用");
        }
        if (account.getLockExpireTime() != null && account.getLockExpireTime().after(new Date()))
        {
            throw new ServiceException("账号已锁定，请联系管理员");
        }
        accountMapper.updateLoginSuccess(account.getId(), loginIp);
        account.setLoginFailCount(0);
        account.setLockExpireTime(null);
        return account;
    }

    /** 修改当前外管账号密码 */
    @Override
    public void changePassword(Long id, String oldPassword, String newPassword)
    {
        ExternalManagerAccount account = selectAccountById(id);
        if (!passwordEncoder.matches(oldPassword, account.getPassword()))
        {
            throw new ServiceException("原密码错误");
        }
        validatePassword(newPassword);
        accountMapper.updatePassword(id, passwordEncoder.encode(newPassword));
    }

    /** 查询角色默认权限 */
    @Override
    public ExternalManagerRolePermissionRequest selectRolePermissions(Integer regionId)
    {
        ExternalManagerRolePermissionRequest result = new ExternalManagerRolePermissionRequest();
        result.setRegionId(regionId);
        List<ExternalManagerRolePermission> items = rolePermissionMapper.selectByRegionId(regionId);
        result.setSuperAdminPermissions(findPermissions(items, ExternalManagerRole.SUPER_ADMIN));
        result.setBdLeaderPermissions(findPermissions(items, ExternalManagerRole.BD_LEADER));
        result.setBdPermissions(findPermissions(items, ExternalManagerRole.BD));
        return result;
    }

    /** 保存角色默认权限 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRolePermissions(ExternalManagerRolePermissionRequest request)
    {
        validateRegion(request.getRegionId());
        List<ExternalManagerRolePermission> items = new ArrayList<>();
        addRolePermissions(items, request.getRegionId(), ExternalManagerRole.SUPER_ADMIN, request.getSuperAdminPermissions());
        addRolePermissions(items, request.getRegionId(), ExternalManagerRole.BD_LEADER, request.getBdLeaderPermissions());
        addRolePermissions(items, request.getRegionId(), ExternalManagerRole.BD, request.getBdPermissions());
        rolePermissionMapper.deleteByRegionId(request.getRegionId());
        if (!items.isEmpty())
        {
            rolePermissionMapper.insertBatch(items);
        }
    }

    /** 记录失败登录并按阈值锁定 */
    private void recordLoginFailure(ExternalManagerAccount account)
    {
        int failureCount = account.getLoginFailCount() + 1;
        Date lockExpireTime = failureCount >= LOGIN_MAX_FAILURE_COUNT ? new Date(System.currentTimeMillis() + LOGIN_LOCK_MILLIS) : null;
        accountMapper.updateLoginFailure(account.getId(), failureCount, lockExpireTime);
    }

    /** 校验保存参数 */
    private ExternalWohaUser validateRequest(ExternalManagerSaveRequest request, Long accountId, boolean create)
    {
        if (create)
        {
            if (StringUtils.isEmpty(request.getPassword()))
            {
                throw new ServiceException("登录密码不能为空");
            }
            validatePassword(request.getPassword());
            if (accountMapper.selectAccountByLoginName(request.getLoginName()) != null)
            {
                throw new ServiceException("登录账号已存在");
            }
        }
        validateRole(request.getRoleCode());
        validateRegion(request.getRegionId());
        ExternalWohaUser user = wohaUserService.selectByChatNo(request.getWohaChatNo());
        if (user == null)
        {
            throw new ServiceException("关联 Woha 用户不存在");
        }
        if (!request.getRegionId().equals(user.getRegionId()))
        {
            throw new ServiceException("关联 Woha 用户与外管账号大区不一致");
        }
        List<ExternalManagerAccount> boundAccounts = accountMapper.selectAccountList(null, user.getUid(), null, null, null, null, null);
        if (boundAccounts.stream().anyMatch(item -> !item.getId().equals(accountId)))
        {
            throw new ServiceException("该 Woha 用户已关联其他外管账号");
        }
        validateParent(request.getRoleCode(), request.getParentId(), request.getRegionId(), accountId);
        validatePermissions(request.getPermissions());
        return user;
    }

    /** 构建外管账号 */
    private ExternalManagerAccount buildAccount(ExternalManagerSaveRequest request, String operator, ExternalWohaUser user)
    {
        ExternalManagerAccount account = new ExternalManagerAccount();
        account.setLoginName(request.getLoginName());
        account.setRealName(request.getRealName());
        account.setWohaUid(user.getUid());
        account.setRoleCode(request.getRoleCode());
        account.setParentId(request.getParentId());
        account.setRegionId(request.getRegionId());
        account.setPermissions(joinPermissions(request.getPermissions()));
        account.setCreateBy(operator);
        account.setUpdateBy(operator);
        return account;
    }

    /** 校验角色和上级关系 */
    private void validateParent(String roleCode, Long parentId, Integer regionId, Long accountId)
    {
        ExternalManagerRole role = ExternalManagerRole.valueOf(roleCode);
        if (role == ExternalManagerRole.SUPER_ADMIN)
        {
            if (parentId != null)
            {
                throw new ServiceException("Super Admin 不允许设置上级");
            }
            return;
        }
        if (parentId == null)
        {
            throw new ServiceException("该角色必须设置上级");
        }
        if (parentId.equals(accountId))
        {
            throw new ServiceException("外管账号不能设置自己为上级");
        }
        ExternalManagerAccount parent = selectAccountById(parentId);
        if (!regionId.equals(parent.getRegionId()))
        {
            throw new ServiceException("上级账号必须属于同一大区");
        }
        if (role == ExternalManagerRole.BD_LEADER && !ExternalManagerRole.SUPER_ADMIN.name().equals(parent.getRoleCode()))
        {
            throw new ServiceException("BD Leader 的上级必须为 Super Admin");
        }
        if (role == ExternalManagerRole.BD && !ExternalManagerRole.SUPER_ADMIN.name().equals(parent.getRoleCode())
                && !ExternalManagerRole.BD_LEADER.name().equals(parent.getRoleCode()))
        {
            throw new ServiceException("BD 的上级必须为 Super Admin 或 BD Leader");
        }
    }

    /** 校验密码规则 */
    private void validatePassword(String password)
    {
        if (password == null || !password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,50}$"))
        {
            throw new ServiceException("密码至少 8 位，且必须包含字母和数字");
        }
    }

    /** 校验固定角色 */
    private void validateRole(String roleCode)
    {
        try { ExternalManagerRole.valueOf(roleCode); } catch (Exception e) { throw new ServiceException("角色不合法"); }
    }

    /** 校验大区编号 */
    private void validateRegion(Integer regionId)
    {
        if (regionId == null || regionId < 1 || regionId > 7)
        {
            throw new ServiceException("大区不合法");
        }
    }

    /** 校验功能权限 */
    private void validatePermissions(List<String> permissions)
    {
        if (permissions == null) return;
        for (String permission : permissions)
        {
            try { ExternalManagerPermission.valueOf(permission); } catch (Exception e) { throw new ServiceException("功能权限不合法"); }
        }
    }

    /** 拼接权限编码 */
    private String joinPermissions(List<String> permissions)
    {
        if (permissions == null || permissions.isEmpty()) return "";
        return permissions.stream().distinct().collect(Collectors.joining(","));
    }

    /** 查找角色权限 */
    private List<String> findPermissions(List<ExternalManagerRolePermission> items, ExternalManagerRole role)
    {
        return items.stream().filter(item -> role.name().equals(item.getRoleCode())).map(ExternalManagerRolePermission::getPermissionCode).collect(Collectors.toList());
    }

    /** 追加角色权限 */
    private void addRolePermissions(List<ExternalManagerRolePermission> items, Integer regionId, ExternalManagerRole role, List<String> permissions)
    {
        validatePermissions(permissions);
        if (permissions == null) return;
        for (String permission : new HashSet<>(permissions))
        {
            ExternalManagerRolePermission item = new ExternalManagerRolePermission();
            item.setRegionId(regionId);
            item.setRoleCode(role.name());
            item.setPermissionCode(permission);
            items.add(item);
        }
    }
}
