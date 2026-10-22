package com.woha.control.web.controller.external;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.woha.control.common.core.controller.BaseController;
import com.woha.control.common.core.domain.AjaxResult;
import com.woha.control.common.core.domain.model.ExternalLoginUser;
import com.woha.control.common.core.domain.model.LoginUser;
import com.woha.control.common.core.page.TableDataInfo;
import com.woha.control.common.exception.ServiceException;
import com.woha.control.common.utils.ip.IpUtils;
import com.woha.control.framework.security.filter.ExternalAuthenticationTokenFilter;
import com.woha.control.framework.web.service.ExternalTokenService;
import com.woha.control.system.domain.external.ExternalLoginRequest;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalManagerRolePermissionRequest;
import com.woha.control.system.domain.external.ExternalManagerSaveRequest;
import com.woha.control.system.domain.external.ExternalPasswordChangeRequest;
import com.woha.control.system.domain.external.ExternalWohaUser;
import com.woha.control.system.service.IExternalManagerAccountService;
import com.woha.control.system.service.IExternalWohaUserService;

/**
 * 外管账号管理
 *
 * @author luowenshen
 */
@RestController
@RequestMapping("/external")
public class ExternalManagerController extends BaseController
{
    private final IExternalManagerAccountService accountService;
    private final IExternalWohaUserService wohaUserService;
    private final ExternalTokenService externalTokenService;

    public ExternalManagerController(IExternalManagerAccountService accountService,
            IExternalWohaUserService wohaUserService, ExternalTokenService externalTokenService)
    {
        this.accountService = accountService;
        this.wohaUserService = wohaUserService;
        this.externalTokenService = externalTokenService;
    }

    /** 外管账号登录 */
    @PostMapping("/auth/login")
    public AjaxResult login(@Valid @RequestBody ExternalLoginRequest request, HttpServletRequest httpRequest)
    {
        ExternalManagerAccount account = accountService.login(request.getLoginName(), request.getPassword(), IpUtils.getIpAddr());
        String token = externalTokenService.createToken(new ExternalLoginUser(account.getId(), account.getLoginName(), account.getPwdUpdateDate() == null));
        AjaxResult result = AjaxResult.success();
        result.put("token", token);
        result.put("needChangePassword", account.getPwdUpdateDate() == null);
        return result;
    }

    /** 外管账号退出登录 */
    @PostMapping("/auth/logout")
    public AjaxResult logout(HttpServletRequest request)
    {
        externalTokenService.invalidateToken(request.getHeader(ExternalAuthenticationTokenFilter.TOKEN_HEADER));
        return AjaxResult.success();
    }

    /** 获取当前外管账号信息 */
    @GetMapping("/auth/profile")
    public AjaxResult profile()
    {
        ExternalManagerAccount account = accountService.selectAccountById(currentAccountId());
        enrichWohaUsers(Collections.singletonList(account));
        account.setPassword(null);
        return AjaxResult.success(account);
    }

    /** 修改当前外管账号密码 */
    @PutMapping("/auth/password")
    public AjaxResult changePassword(@Valid @RequestBody ExternalPasswordChangeRequest request, HttpServletRequest httpRequest)
    {
        Long accountId = currentAccountId();
        accountService.changePassword(accountId, request.getOldPassword(), request.getNewPassword());
        externalTokenService.invalidateAccountTokens(accountId);
        return AjaxResult.success();
    }

    /** 分页查询外管账号 */
    @GetMapping("/accounts/list")
    public TableDataInfo list(@RequestParam(required = false) String loginName, @RequestParam(required = false) Long wohaChatNo,
            @RequestParam(required = false) String roleCode, @RequestParam(required = false) Integer regionId,
            @RequestParam(required = false) String status, @RequestParam(required = false) String parentLoginName,
            @RequestParam(required = false) String openMethod)
    {
        Long wohaUid = null;
        if (wohaChatNo != null)
        {
            ExternalWohaUser user = wohaUserService.selectByChatNo(wohaChatNo);
            wohaUid = user == null ? -1L : user.getUid();
        }
        startPage();
        List<ExternalManagerAccount> list = accountService.selectAccountList(loginName, wohaUid, roleCode, regionId, status, parentLoginName, openMethod);
        enrichWohaUsers(list);
        list.forEach(item -> item.setPassword(null));
        return getDataTable(list);
    }

    /** 查询外管账号详情 */
    @GetMapping("/accounts/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        ExternalManagerAccount account = accountService.selectAccountById(id);
        enrichWohaUsers(Collections.singletonList(account));
        account.setPassword(null);
        return AjaxResult.success(account);
    }

    /** 新增外管账号 */
    @PostMapping("/accounts")
    public AjaxResult add(@Valid @RequestBody ExternalManagerSaveRequest request)
    {
        accountService.createAccount(request, currentLoginName(), currentOpenMethod());
        return AjaxResult.success();
    }

    /** 修改外管账号 */
    @PutMapping("/accounts/{id}")
    public AjaxResult edit(@PathVariable Long id, @Valid @RequestBody ExternalManagerSaveRequest request)
    {
        accountService.updateAccount(id, request, currentLoginName());
        return AjaxResult.success();
    }

    /** 停用或启用外管账号 */
    @PutMapping("/accounts/{id}/status")
    public AjaxResult changeStatus(@PathVariable Long id, @RequestBody Map<String, String> request)
    {
        accountService.updateAccountStatus(id, request.get("status"));
        return AjaxResult.success();
    }

    /** 删除外管账号 */
    @DeleteMapping("/accounts/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        accountService.deleteAccount(id);
        externalTokenService.invalidateAccountTokens(id);
        return AjaxResult.success();
    }

    /** 重置外管账号密码 */
    @PutMapping("/accounts/{id}/password")
    public AjaxResult resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request)
    {
        accountService.resetPassword(id, request.get("password"));
        externalTokenService.invalidateAccountTokens(id);
        return AjaxResult.success();
    }

    /** 查询角色默认权限 */
    @GetMapping("/role-permissions")
    public AjaxResult getRolePermissions(@RequestParam Integer regionId)
    {
        return AjaxResult.success(accountService.selectRolePermissions(regionId));
    }

    /** 保存角色默认权限 */
    @PutMapping("/role-permissions")
    public AjaxResult saveRolePermissions(@Valid @RequestBody ExternalManagerRolePermissionRequest request)
    {
        accountService.saveRolePermissions(request);
        return AjaxResult.success();
    }

    /** 批量补齐关联 Woha 用户展示账号 */
    private void enrichWohaUsers(List<ExternalManagerAccount> accounts)
    {
        List<Long> uids = accounts.stream().map(ExternalManagerAccount::getWohaUid).distinct().collect(Collectors.toList());
        if (uids.isEmpty()) return;
        Map<Long, ExternalWohaUser> users = wohaUserService.selectByUids(uids).stream()
                .collect(Collectors.toMap(ExternalWohaUser::getUid, Function.identity()));
        accounts.forEach(item -> {
            ExternalWohaUser user = users.get(item.getWohaUid());
            if (user != null) item.setWohaChatNo(user.getChatNo());
        });
    }

    /** 获取当前外管账号主键 */
    private Long currentAccountId()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof ExternalLoginUser))
        {
            throw new ServiceException("外管登录状态无效");
        }
        return ((ExternalLoginUser) authentication.getPrincipal()).getAccountId();
    }

    /** 获取当前外管登录账号 */
    private String currentLoginName()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
        {
            throw new ServiceException("登录状态无效");
        }
        if (authentication.getPrincipal() instanceof ExternalLoginUser)
        {
            return ((ExternalLoginUser) authentication.getPrincipal()).getLoginName();
        }
        if (authentication.getPrincipal() instanceof LoginUser)
        {
            return ((LoginUser) authentication.getPrincipal()).getUsername();
        }
        throw new ServiceException("登录状态无效");
    }

    /** 获取当前账号对应的开通方式 */
    private String currentOpenMethod()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof ExternalLoginUser)
        {
            return ((ExternalLoginUser) authentication.getPrincipal()).getLoginName() + " 新增";
        }
        return "运营开通";
    }
}
