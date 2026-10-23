package com.woha.control.web.controller.external;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import com.github.pagehelper.PageHelper;
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
import com.woha.control.common.utils.poi.ExcelUtil;
import com.woha.control.framework.security.filter.ExternalAuthenticationTokenFilter;
import com.woha.control.framework.web.service.ExternalTokenService;
import com.woha.control.system.domain.external.ExternalLoginRequest;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalManagerRolePermissionRequest;
import com.woha.control.system.domain.external.ExternalManagerSaveRequest;
import com.woha.control.system.domain.external.ExternalPasswordChangeRequest;
import com.woha.control.system.domain.external.ExternalWohaUser;
import com.woha.control.system.domain.external.ExternalSubordinateCreateRequest;
import com.woha.control.system.domain.external.ExternalTeamMetric;
import com.woha.control.system.domain.external.ExternalGuildCreateRequest;
import com.woha.control.system.domain.external.ExternalGuildFlowSummary;
import com.woha.control.system.domain.external.ExternalGuildQuery;
import com.woha.control.system.domain.external.ExternalGuildVO;
import com.woha.control.system.service.IExternalGuildService;
import com.woha.control.system.service.IExternalManagerTeamService;
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
    private final IExternalManagerTeamService teamService;
    private final IExternalGuildService guildService;

    public ExternalManagerController(IExternalManagerAccountService accountService,
            IExternalWohaUserService wohaUserService, ExternalTokenService externalTokenService,
            IExternalManagerTeamService teamService, IExternalGuildService guildService)
    {
        this.accountService = accountService;
        this.wohaUserService = wohaUserService;
        this.externalTokenService = externalTokenService;
        this.teamService = teamService;
        this.guildService = guildService;
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

    /** 查询外管工作台 */
    @GetMapping("/workbench")
    public AjaxResult workbench()
    {
        ExternalManagerAccount current = accountService.selectAccountById(currentAccountId());
        List<ExternalManagerAccount> accounts = teamService.selectTeamMembers(teamService.resolveVisibleAccountIds(current), null, null, null);
        Map<Long, ExternalTeamMetric> metrics = teamService.selectMetrics(accounts).stream().collect(Collectors.toMap(ExternalTeamMetric::getWohaUid, Function.identity()));
        long guildCount = metrics.values().stream().mapToLong(item -> item.getGuildCount() == null ? 0 : item.getGuildCount()).sum();
        long dayFlow = metrics.values().stream().mapToLong(item -> item.getDayFlow() == null ? 0 : item.getDayFlow()).sum();
        long weekFlow = metrics.values().stream().mapToLong(item -> item.getWeekFlow() == null ? 0 : item.getWeekFlow()).sum();
        long monthFlow = metrics.values().stream().mapToLong(item -> item.getMonthFlow() == null ? 0 : item.getMonthFlow()).sum();
        long totalFlow = metrics.values().stream().mapToLong(item -> item.getTotalFlow() == null ? 0 : item.getTotalFlow()).sum();
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("bdLeaderCount", accounts.stream().filter(item -> "BD_LEADER".equals(item.getRoleCode())).count()); result.put("bdCount", accounts.stream().filter(item -> "BD".equals(item.getRoleCode())).count()); result.put("guildCount", guildCount); result.put("anchorCount", 0); result.put("dayFlow", dayFlow); result.put("weekFlow", weekFlow); result.put("monthFlow", monthFlow); result.put("totalFlow", totalFlow);
        return AjaxResult.success(result);
    }

    /** 分页查询当前外管可见团队成员 */
    @GetMapping("/team/members")
    public TableDataInfo teamMembers(@RequestParam(required = false) String loginName, @RequestParam(required = false) String roleCode, @RequestParam(required = false) String status)
    {
        ExternalManagerAccount current = accountService.selectAccountById(currentAccountId()); List<Long> ids = teamService.resolveVisibleAccountIds(current);
        startPage(); List<ExternalManagerAccount> list = teamService.selectTeamMembers(ids, loginName, roleCode, status); enrichWohaUsers(list);
        Map<Long, ExternalTeamMetric> metrics = teamService.selectMetrics(list).stream().collect(Collectors.toMap(ExternalTeamMetric::getWohaUid, Function.identity()));
        list.forEach(item -> { ExternalTeamMetric metric = metrics.get(item.getWohaUid()); if (metric != null) { item.setGuildCount(metric.getGuildCount()); item.setMonthFlow(metric.getMonthFlow()); } item.setPassword(null); });
        return getDataTable(list);
    }

    /** 新增当前外管账号下级 */
    @PostMapping("/team/subordinates")
    public AjaxResult addSubordinate(@Valid @RequestBody ExternalSubordinateCreateRequest request)
    {
        String password = accountService.createSubordinate(accountService.selectAccountById(currentAccountId()), request); AjaxResult result = AjaxResult.success(); result.put("initialPassword", password); return result;
    }

    /** 查询当前账号可选择的公会负责人 */
    @GetMapping("/guild/responsibles")
    public AjaxResult guildResponsibles()
    {
        ExternalManagerAccount current = accountService.selectAccountById(currentAccountId());
        List<ExternalManagerAccount> accounts = selectGuildResponsibleAccounts(current);
        enrichWohaUsers(accounts);
        accounts.forEach(item -> item.setPassword(null));
        return AjaxResult.success(accounts);
    }

    /** 创建外管公会 */
    @PostMapping("/guild")
    public AjaxResult createGuild(@Valid @RequestBody ExternalGuildCreateRequest request)
    {
        ExternalManagerAccount current = accountService.selectAccountById(currentAccountId());
        ExternalManagerAccount responsible = selectGuildResponsibleAccounts(current).stream()
                .filter(item -> item.getId().equals(request.getResponsibleAccountId())).findFirst()
                .orElseThrow(() -> new ServiceException("负责人不在当前账号可选范围内"));
        Long familyId = guildService.createGuild(request, responsible);
        return AjaxResult.success("创建成功", Collections.singletonMap("familyId", familyId));
    }

    /** 分页查询当前外管可见公会 */
    @GetMapping("/guild/list")
    public TableDataInfo guildList(ExternalGuildQuery query)
    {
        ExternalManagerAccount current = accountService.selectAccountById(currentAccountId());
        List<ExternalManagerAccount> accounts = selectVisibleGuildAccounts(current);
        query.setVisibleWohaUids(accounts.stream().map(ExternalManagerAccount::getWohaUid).collect(Collectors.toList()));
        startPage();
        List<ExternalGuildVO> list = guildService.selectGuildList(query);
        enrichGuildAccounts(list, accounts);
        return getDataTable(list);
    }

    /** 查询当前外管可见公会流水汇总 */
    @GetMapping("/guild/flow-summary")
    public AjaxResult guildFlowSummary(ExternalGuildQuery query)
    {
        ExternalManagerAccount current = accountService.selectAccountById(currentAccountId());
        List<ExternalManagerAccount> accounts = selectVisibleGuildAccounts(current);
        query.setVisibleWohaUids(accounts.stream().map(ExternalManagerAccount::getWohaUid).collect(Collectors.toList()));
        ExternalGuildFlowSummary summary = guildService.selectGuildFlowSummary(query);
        return AjaxResult.success(summary);
    }

    /** 导出当前外管可见公会流水，最多一万条 */
    @PostMapping("/guild/export")
    public void exportGuild(HttpServletResponse response, ExternalGuildQuery query)
    {
        ExternalManagerAccount current = accountService.selectAccountById(currentAccountId());
        List<ExternalManagerAccount> accounts = selectVisibleGuildAccounts(current);
        query.setVisibleWohaUids(accounts.stream().map(ExternalManagerAccount::getWohaUid).collect(Collectors.toList()));
        PageHelper.startPage(1, 10000, false);
        List<ExternalGuildVO> list = guildService.selectGuildList(query);
        enrichGuildAccounts(list, accounts);
        new ExcelUtil<>(ExternalGuildVO.class).exportExcel(response, list, "公会流水");
    }

    /** 修改当前外管账号密码 */
    @PutMapping("/auth/password")
    public AjaxResult changePassword(@Valid @RequestBody ExternalPasswordChangeRequest request, HttpServletRequest httpRequest)
    {
        List<ExternalManagerAccount> accounts = accountService.selectAccountList(currentLoginName(), null, null, null, null, null, null);
        if (accounts.isEmpty())
        {
            throw new ServiceException("外管账号不存在");
        }
        accountService.changePassword(accounts.get(0).getId(), request.getOldPassword(), request.getNewPassword());
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

    /** 查询当前账号可查看的有效 BD 外管账号 */
    private List<ExternalManagerAccount> selectVisibleGuildAccounts(ExternalManagerAccount current)
    {
        List<Long> ids = teamService.resolveVisibleAccountIds(current);
        return new ArrayList<>(teamService.selectTeamMembers(ids, null, null, "0"));
    }

    /** 按外管角色收敛建会负责人范围 */
    private List<ExternalManagerAccount> selectGuildResponsibleAccounts(ExternalManagerAccount current)
    {
        List<ExternalManagerAccount> accounts = new ArrayList<>(teamService.selectTeamMembers(
                teamService.resolveVisibleAccountIds(current), null, null, "0"));
        if ("BD".equals(current.getRoleCode()))
        {
            accounts.removeIf(item -> !current.getId().equals(item.getId()));
        }
        else if ("BD_LEADER".equals(current.getRoleCode()))
        {
            accounts.removeIf(item -> !current.getId().equals(item.getId())
                    && !(current.getId().equals(item.getParentId()) && "BD".equals(item.getRoleCode())));
        }
        return accounts;
    }

    /** 补齐公会列表中的外管负责人展示名称 */
    private void enrichGuildAccounts(List<ExternalGuildVO> guilds, List<ExternalManagerAccount> accounts)
    {
        Map<Long, ExternalManagerAccount> accountByUid = accounts.stream().collect(Collectors.toMap(
                ExternalManagerAccount::getWohaUid, Function.identity(), (first, ignored) -> first));
        Map<Long, ExternalManagerAccount> accountById = accounts.stream().collect(Collectors.toMap(
                ExternalManagerAccount::getId, Function.identity(), (first, ignored) -> first));
        guilds.forEach(item -> {
            ExternalManagerAccount responsible = accountByUid.get(item.getResponsibleWohaUid());
            ExternalManagerAccount leader = responsible == null ? null : accountById.get(responsible.getParentId());
            item.setResponsibleName(responsible == null ? "--" : responsible.getRealName());
            item.setBdLeaderName(leader == null ? "--" : leader.getRealName());
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
        ExternalLoginUser loginUser = (ExternalLoginUser) authentication.getPrincipal();
        List<ExternalManagerAccount> accounts = accountService.selectAccountList(loginUser.getLoginName(), null, null, null, null, null, null);
        if (accounts.isEmpty())
        {
            throw new ServiceException("外管账号不存在");
        }
        return accounts.get(0).getId();
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
