package com.woha.business.web.controller.bi;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.woha.business.bi.domain.InviteGrowthQuery;
import com.woha.business.bi.domain.InviteQualityVO;
import com.woha.business.bi.service.IInviteGrowthDashboardService;
import com.woha.business.common.annotation.Log;
import com.woha.business.common.core.controller.BaseController;
import com.woha.business.common.core.domain.AjaxResult;
import com.woha.business.common.core.page.TableDataInfo;
import com.woha.business.common.enums.BusinessType;
import com.woha.business.common.utils.poi.ExcelUtil;

/**
 * 邀请增长看板接口。
 *
 * @author luowenshen
 */
@Tag(name = "邀请增长看板")
@RestController
@RequestMapping("/bi/invite-growth")
public class InviteGrowthDashboardController extends BaseController
{
    /** 邀请增长看板服务 */
    private final IInviteGrowthDashboardService dashboardService;

    public InviteGrowthDashboardController(IInviteGrowthDashboardService dashboardService)
    {
        this.dashboardService = dashboardService;
    }

    /** 查询邀请增长看板聚合数据。 */
    @Operation(summary = "查询邀请增长看板聚合数据")
    @PreAuthorize("@ss.hasPermi('bi:inviteGrowth:list')")
    @GetMapping
    public AjaxResult getDashboard(InviteGrowthQuery query)
    {
        return success(dashboardService.selectDashboard(query));
    }

    /** 查询邀请人质量分层列表。 */
    @Operation(summary = "查询邀请人质量分层列表")
    @PreAuthorize("@ss.hasPermi('bi:inviteGrowth:list')")
    @GetMapping("/quality/list")
    public TableDataInfo qualityList(InviteGrowthQuery query)
    {
        startPage();
        return getDataTable(dashboardService.selectInviteQuality(query));
    }

    /** 导出邀请人质量分层数据。 */
    @Operation(summary = "导出邀请人质量分层数据")
    @Log(title = "邀请人质量分层", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('bi:inviteGrowth:export')")
    @PostMapping("/quality/export")
    public void exportQuality(HttpServletResponse response, InviteGrowthQuery query)
    {
        List<InviteQualityVO> list = dashboardService.selectInviteQuality(query);
        ExcelUtil<InviteQualityVO> util = new ExcelUtil<>(InviteQualityVO.class);
        util.exportExcel(response, list, "邀请人质量分层");
    }

    /** 查询邀请增长看板筛选维度。 */
    @Operation(summary = "查询邀请增长看板筛选维度")
    @PreAuthorize("@ss.hasPermi('bi:inviteGrowth:list')")
    @GetMapping("/dimensions")
    public AjaxResult getDimensions(@RequestParam(required = false) Integer regionId)
    {
        return success(dashboardService.selectDimensions(regionId));
    }
}
