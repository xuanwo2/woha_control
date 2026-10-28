package com.woha.control.web.controller.external;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import com.github.pagehelper.PageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.woha.control.system.domain.external.ExternalManagerOpenMethod;
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
import com.woha.control.system.domain.external.ExternalGiftPack;
import com.woha.control.system.domain.external.ExternalGiftPackIssueRequest;
import com.woha.control.system.domain.external.ExternalGiftPackQuotaSaveRequest;
import com.woha.control.system.domain.external.ExternalChatNoAssignRequest;
import com.woha.control.system.domain.external.ExternalChatNoPoolSaveRequest;
import com.woha.control.system.domain.external.ExternalAccountBanRequest;
import com.woha.control.system.domain.external.ExternalOperationLog;
import com.woha.control.system.domain.external.ExternalManagerPermission;
import com.woha.control.system.service.IExternalAccountBanService;
import com.woha.control.system.service.ExternalOperationLogService;
import com.woha.control.system.service.IExternalGiftPackService;
import com.woha.control.system.service.IExternalChatNoService;
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
    private final IExternalGiftPackService giftPackService;
    private final IExternalChatNoService chatNoService;
    private final IExternalAccountBanService accountBanService;
    private final ExternalOperationLogService operationLogService;

    public ExternalManagerController(IExternalManagerAccountService accountService,
            IExternalWohaUserService wohaUserService, ExternalTokenService externalTokenService,
            IExternalManagerTeamService teamService, IExternalGuildService guildService,
            IExternalGiftPackService giftPackService, IExternalChatNoService chatNoService, IExternalAccountBanService accountBanService, ExternalOperationLogService operationLogService)
    {
        this.accountService = accountService;
        this.wohaUserService = wohaUserService;
        this.externalTokenService = externalTokenService;
        this.teamService = teamService;
        this.guildService = guildService;
        this.giftPackService = giftPackService;
        this.chatNoService = chatNoService;
        this.accountBanService = accountBanService;
        this.operationLogService = operationLogService;
    }

    /** 查询外管礼包 */
    @GetMapping("/gift-packs")
    public AjaxResult giftPackList(@RequestParam(required = false) String status, @RequestParam(required = false) Integer regionId)
    {
        if (isInternalAdmin())
        {
            return AjaxResult.success(giftPackService.selectGiftPackList(status, regionId));
        }
        ExternalManagerAccount account = accountService.selectAccountById(currentAccountId());
        return AjaxResult.success(giftPackService.selectGiftPackList(status, account.getRegionId()));
    }

    /** 保存外管礼包 */
    @PostMapping("/gift-packs")
    public AjaxResult saveGiftPack(@Valid @RequestBody ExternalGiftPack giftPack)
    {
        ensureInternalAdmin();
        giftPackService.saveGiftPack(giftPack, currentLoginName());
        return AjaxResult.success();
    }

    /** 查询外管账号礼包额度 */
    @GetMapping("/gift-packs/quotas")
    public AjaxResult giftPackQuotaList(@RequestParam(required = false) Long accountId,
            @RequestParam(defaultValue = "false") boolean all)
    {
        if (isInternalAdmin())
        {
            return AjaxResult.success(giftPackService.selectQuotaList(all ? null : accountId));
        }
        Long currentAccountId = currentAccountId();
        if (accountId != null && !accountId.equals(currentAccountId))
        {
            throw new ServiceException("不能查询其他外管账号的礼包额度");
        }
        return AjaxResult.success(giftPackService.selectQuotaList(accountId == null ? currentAccountId : accountId));
    }

    /** 保存外管账号礼包额度 */
    @PutMapping("/gift-packs/quotas")
    public AjaxResult saveGiftPackQuota(@Valid @RequestBody ExternalGiftPackQuotaSaveRequest request)
    {
        ensureInternalAdmin();
        ExternalManagerAccount account = accountService.selectAccountById(request.getAccountId());
        giftPackService.saveQuota(request, account.getRegionId());
        return AjaxResult.success();
    }

    /** 赠送外管礼包 */
    @PostMapping("/gift-packs/issue")
    public AjaxResult issueGiftPack(@Valid @RequestBody ExternalGiftPackIssueRequest request)
    {
        ExternalManagerAccount account = accountService.selectAccountById(currentAccountId());
        giftPackService.issueGiftPack(account, request);
        operationLogService.record(account, "ISSUE_GIFT", String.valueOf(request.getReceiverChatNo()), "赠送礼包");
        return AjaxResult.success();
    }

    /** 查询当前外管账号的礼包赠送记录 */
    @GetMapping("/gift-packs/issues")
    public AjaxResult giftPackIssueList()
    {
        return AjaxResult.success(giftPackService.selectIssueList(currentAccountId()));
    }

    /** 查询当前外管账号的靓号池。 */
    @GetMapping("/chat-nos")
    public AjaxResult chatNoList()
    {
        return AjaxResult.success(chatNoService.selectPoolList(currentAccountId()));
    }

    /** 分配靓号给用户。 */
    @PostMapping("/chat-nos/assign")
    public AjaxResult assignChatNo(@Valid @RequestBody ExternalChatNoAssignRequest request)
    {
        ExternalManagerAccount account = accountService.selectAccountById(currentAccountId()); chatNoService.assign(account, request);
        operationLogService.record(account, "ASSIGN_CHAT_NO", String.valueOf(request.getReceiverChatNo()), request.getChatNo() + "，" + request.getDayNum() + "天");
        return AjaxResult.success();
    }

    /** 封禁团队公会成员。 */
    @PostMapping("/account-bans")
    public AjaxResult banAccount(@Valid @RequestBody ExternalAccountBanRequest request)
    {
        accountBanService.ban(accountService.selectAccountById(currentAccountId()), request);
        return AjaxResult.success();
    }

    /** 提前解除团队公会成员封禁。 */
    @PostMapping("/account-bans/{chatNo}/release")
    public AjaxResult releaseAccountBan(@PathVariable Long chatNo)
    {
        accountBanService.release(accountService.selectAccountById(currentAccountId()), chatNo);
        return AjaxResult.success();
    }

    /** 查询当前外管账号的封禁处理记录。 */
    @GetMapping("/account-bans/records")
    public AjaxResult accountBanRecords()
    {
        ExternalManagerAccount current = accountService.selectAccountById(currentAccountId());
        List<ExternalOperationLog> records = operationLogService.selectList(current.getLoginName(), null, null, current.getRegionId(), null, null)
                .stream().filter(item -> "BAN".equals(item.getOperationType()) || "UNBAN".equals(item.getOperationType()))
                .collect(Collectors.toList());
        return AjaxResult.success(records);
    }

    /** 管理后台查询外管操作记录。 */
    @GetMapping("/operation-logs")
    public TableDataInfo operationLogList(@RequestParam(required = false) String loginName, @RequestParam(required = false) String superAdminName,
            @RequestParam(required = false) String operationType, @RequestParam(required = false) Integer regionId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime)
    {
        ensureInternalAdmin(); startPage(); return getDataTable(operationLogService.selectList(loginName, superAdminName, operationType, regionId, beginTime, endTime));
    }

    /** 管理后台导出外管操作记录。 */
    @PostMapping("/operation-logs/export")
    public void exportOperationLogs(HttpServletResponse response, @RequestParam(required = false) String loginName,
            @RequestParam(required = false) String superAdminName, @RequestParam(required = false) String operationType,
            @RequestParam(required = false) Integer regionId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime)
    {
        ensureInternalAdmin();
        PageHelper.startPage(1, 10000, false);
        List<ExternalOperationLog> list = operationLogService.selectList(loginName, superAdminName, operationType, regionId, beginTime, endTime);
        new ExcelUtil<>(ExternalOperationLog.class).exportExcel(response, list, "外管操作记录");
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
        result.put("giftPackQuotas", hasPermission(current, ExternalManagerPermission.SEND_GIFT_PACK)
                ? giftPackService.selectQuotaList(current.getId()) : Collections.emptyList());
        if (hasPermission(current, ExternalManagerPermission.ASSIGN_CHAT_NO))
        {
            result.put("chatNoTotal", chatNoService.countPoolByAccountId(current.getId()));
            result.put("chatNoAvailable", chatNoService.countAvailablePoolByAccountId(current.getId()));
        }
        if (hasPermission(current, ExternalManagerPermission.EXECUTE_BAN))
        {
            result.put("todayBanCount", operationLogService.countToday(current.getId(), "BAN"));
        }
        result.put("recentOperations", operationLogService.selectLatestByAccountId(current.getId(), 5));
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
        ExternalManagerAccount account = accountService.selectAccountById(currentAccountId()); String password = accountService.createSubordinate(account, request); operationLogService.record(account, "CREATE_SUBORDINATE", request.getLoginName(), request.getRoleCode()); AjaxResult result = AjaxResult.success(); result.put("initialPassword", password); return result;
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

    /** 查询新建公会会长预览信息 */
    @GetMapping("/guild/captain-preview")
    public AjaxResult guildCaptainPreview(@RequestParam Long chatNo)
    {
        ExternalManagerAccount current = accountService.selectAccountById(currentAccountId());
        return AjaxResult.success(guildService.selectCaptainPreview(chatNo, current.getRegionId()));
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
        operationLogService.record(current, "CREATE_GUILD", String.valueOf(familyId), request.getFamilyName());
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
    public void exportGuild(HttpServletResponse response, ExternalGuildQuery query, @RequestParam(required = false) String fieldCodes)
    {
        ExternalManagerAccount current = accountService.selectAccountById(currentAccountId());
        requirePermission(current, ExternalManagerPermission.EXPORT_GUILD_FLOW);
        List<ExternalManagerAccount> accounts = selectVisibleGuildAccounts(current);
        query.setVisibleWohaUids(accounts.stream().map(ExternalManagerAccount::getWohaUid).collect(Collectors.toList()));
        PageHelper.startPage(1, 10000, false);
        List<ExternalGuildVO> list = guildService.selectGuildList(query);
        enrichGuildAccounts(list, accounts);
        ExcelUtil<ExternalGuildVO> excelUtil = new ExcelUtil<>(ExternalGuildVO.class);
        excelUtil.showColumn(selectExportFields(fieldCodes, "familyName", "familyNo", "responsibleName", "bdLeaderName", "rangeFlow", "weekFlow", "monthFlow", "totalFlow", "createTime"));
        excelUtil.exportExcel(response, list, "公会流水");
        operationLogService.record(current, "EXPORT_GUILD_FLOW", "公会流水", "导出" + list.size() + "条");
    }

    /** 导出当前外管可见公会列表。 */
    @PostMapping("/guild/list/export")
    public void exportGuildList(HttpServletResponse response, ExternalGuildQuery query, @RequestParam(required = false) String fieldCodes)
    {
        ExternalManagerAccount current=accountService.selectAccountById(currentAccountId()); requirePermission(current, ExternalManagerPermission.EXPORT_GUILD_LIST); List<ExternalManagerAccount> accounts=selectVisibleGuildAccounts(current); query.setVisibleWohaUids(accounts.stream().map(ExternalManagerAccount::getWohaUid).collect(Collectors.toList())); PageHelper.startPage(1,10000,false); List<ExternalGuildVO> list=guildService.selectGuildList(query); enrichGuildAccounts(list,accounts); ExcelUtil<ExternalGuildVO> excelUtil = new ExcelUtil<>(ExternalGuildVO.class); excelUtil.showColumn(selectExportFields(fieldCodes, "familyName", "familyNo", "captainChatNo", "captainNickname", "responsibleName", "bdLeaderName", "anchorCount", "monthFlow", "createMethod", "statusName", "createTime")); excelUtil.exportExcel(response,list,"公会列表"); operationLogService.record(current,"EXPORT_GUILD_LIST","公会列表","导出"+list.size()+"条");
    }

    /** 导出当前外管可见团队成员。 */
    @PostMapping("/team/members/export")
    public void exportTeamMembers(HttpServletResponse response, @RequestParam(required = false) String fieldCodes)
    {
        ExternalManagerAccount current=accountService.selectAccountById(currentAccountId()); requirePermission(current, ExternalManagerPermission.EXPORT_TEAM_MEMBER); List<ExternalManagerAccount> list=teamService.selectTeamMembers(teamService.resolveVisibleAccountIds(current),null,null,null); enrichWohaUsers(list); ExcelUtil<ExternalManagerAccount> excelUtil = new ExcelUtil<>(ExternalManagerAccount.class); excelUtil.showColumn(selectExportFields(fieldCodes, "loginName", "realName", "wohaUid", "roleCode", "parentLoginName")); excelUtil.exportExcel(response,list,"团队成员"); operationLogService.record(current,"EXPORT_TEAM_MEMBER","团队成员","导出"+list.size()+"条");
    }

    /** 导出当前外管礼包赠送记录。 */
    @PostMapping("/gift-packs/issues/export")
    public void exportGiftIssues(HttpServletResponse response, @RequestParam(required = false) String fieldCodes)
    {
        ExternalManagerAccount current=accountService.selectAccountById(currentAccountId()); requirePermission(current, ExternalManagerPermission.EXPORT_GIFT_ISSUE); List<com.woha.control.system.domain.external.ExternalGiftPackIssue> list=giftPackService.selectIssueList(current.getId()); ExcelUtil<com.woha.control.system.domain.external.ExternalGiftPackIssue> excelUtil = new ExcelUtil<>(com.woha.control.system.domain.external.ExternalGiftPackIssue.class); excelUtil.showColumn(selectExportFields(fieldCodes, "giftName", "receiverUid", "loginName", "contentSnapshot", "status", "createTime")); excelUtil.exportExcel(response,list,"礼包赠送记录"); operationLogService.record(current,"EXPORT_GIFT_ISSUE","礼包赠送记录","导出"+list.size()+"条");
    }

    /** 导出当前外管新增下级账号记录。 */
    @PostMapping("/account-assignments/export")
    public void exportAccountAssignments(HttpServletResponse response)
    {
        ExternalManagerAccount current=accountService.selectAccountById(currentAccountId()); requirePermission(current, ExternalManagerPermission.EXPORT_ACCOUNT_ASSIGNMENT); List<ExternalOperationLog> list=operationLogService.selectList(current.getLoginName(),null,"CREATE_SUBORDINATE",current.getRegionId(),null,null); new ExcelUtil<>(ExternalOperationLog.class).exportExcel(response,list,"账号分配记录"); operationLogService.record(current,"EXPORT_ACCOUNT_ASSIGNMENT","账号分配记录","导出"+list.size()+"条");
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
        operationLogService.record(accounts.get(0), "CHANGE_PASSWORD", "自身账号", "修改密码");
        return AjaxResult.success();
    }

    /** 分页查询外管账号 */
    @GetMapping("/accounts/list")
    public TableDataInfo list(@RequestParam(required = false) String loginName, @RequestParam(required = false) Long wohaChatNo,
            @RequestParam(required = false) String roleCode, @RequestParam(required = false) Integer regionId,
            @RequestParam(required = false) String status, @RequestParam(required = false) String parentLoginName,
            @RequestParam(required = false) ExternalManagerOpenMethod openMethod)
    {
        Long wohaUid = null;
        if (wohaChatNo != null)
        {
            ExternalWohaUser user = wohaUserService.selectByChatNo(wohaChatNo);
            wohaUid = user == null ? -1L : user.getUid();
        }
        startPage();
        List<ExternalManagerAccount> list = accountService.selectAccountList(loginName, wohaUid, roleCode, regionId, status, parentLoginName,
                openMethod == null ? null : openMethod.name());
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
        if (account.getParentId() != null)
        {
            ExternalManagerAccount parent = accountService.selectAccountById(account.getParentId());
            ExternalWohaUser parentUser = wohaUserService.selectByUid(parent.getWohaUid());
            account.setParentWohaChatNo(parentUser == null ? null : parentUser.getChatNo());
        }
        account.setPassword(null);
        return AjaxResult.success(account);
    }

    /** 管理后台查询外管账号靓号池。 */
    @GetMapping("/accounts/{id}/chat-nos")
    public AjaxResult accountChatNoList(@PathVariable Long id)
    {
        ensureInternalAdmin();
        accountService.selectAccountById(id);
        return AjaxResult.success(chatNoService.selectPoolList(id));
    }

    /** 管理后台向外管账号划拨靓号。 */
    @PostMapping("/accounts/chat-nos")
    public AjaxResult addAccountChatNo(@Valid @RequestBody ExternalChatNoPoolSaveRequest request)
    {
        ensureInternalAdmin();
        accountService.selectAccountById(request.getAccountId());
        chatNoService.addPool(request);
        return AjaxResult.success();
    }

    /** 管理后台回收外管账号未分配靓号。 */
    @DeleteMapping("/accounts/{accountId}/chat-nos/{chatNo}")
    public AjaxResult removeAccountChatNo(@PathVariable Long accountId, @PathVariable Long chatNo)
    {
        ensureInternalAdmin();
        chatNoService.removeAvailablePool(accountId, chatNo);
        return AjaxResult.success();
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
        if (loginUser.getAccountId() == null)
        {
            throw new ServiceException("外管登录状态无效");
        }
        return loginUser.getAccountId();
    }

    /** 校验当前外管账号为超级管理员 */
    private void ensureSuperAdmin()
    {
        if (!"SUPER_ADMIN".equals(accountService.selectAccountById(currentAccountId()).getRoleCode()))
        {
            throw new ServiceException("仅 Super Admin 可以配置外管礼包和额度");
        }
    }

    /** 校验管理后台管理员登录态。 */
    private void ensureInternalAdmin()
    {
        if (!isInternalAdmin())
        {
            throw new ServiceException("仅管理后台管理员可以配置礼包和靓号");
        }
    }

    /** 判断当前是否为管理后台管理员。 */
    private boolean isInternalAdmin()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof LoginUser;
    }

    /** 从允许导出的字段中筛选前端提交的列编码。 */
    private String[] selectExportFields(String fieldCodes, String... allowedFields)
    {
        if (fieldCodes == null || fieldCodes.isBlank())
        {
            return allowedFields;
        }
        Set<String> allowed = new HashSet<>(Arrays.asList(allowedFields));
        String[] selected = Arrays.stream(fieldCodes.split(",")).filter(allowed::contains).distinct().toArray(String[]::new);
        return selected.length == 0 ? allowedFields : selected;
    }

    /** 校验外管功能权限。 */
    private void requirePermission(ExternalManagerAccount account, ExternalManagerPermission permission)
    {
        if (account.getPermissions() == null || !List.of(account.getPermissions().split(",")).contains(permission.name())) throw new ServiceException("当前账号没有导出权限");
    }

    /** 判断外管账号是否拥有指定功能权限。 */
    private boolean hasPermission(ExternalManagerAccount account, ExternalManagerPermission permission)
    {
        return account.getPermissions() != null && List.of(account.getPermissions().split(",")).contains(permission.name());
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
            return ExternalManagerOpenMethod.SUBORDINATE_OPEN.name();
        }
        return ExternalManagerOpenMethod.OPERATION_OPEN.name();
    }
}
