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
import com.woha.business.bi.domain.PlayDashboardQuery;
import com.woha.business.bi.domain.PlayGameOverviewVO;
import com.woha.business.bi.service.IPlayDashboardService;
import com.woha.business.common.annotation.Log;
import com.woha.business.common.core.controller.BaseController;
import com.woha.business.common.core.domain.AjaxResult;
import com.woha.business.common.enums.BusinessType;
import com.woha.business.common.utils.poi.ExcelUtil;

/**
 * 玩法看板接口。
 *
 * @author luowenshen
 */
@Tag(name = "玩法看板")
@RestController
@RequestMapping("/bi/play")
public class PlayDashboardController extends BaseController
{
    /** 玩法看板服务 */
    private final IPlayDashboardService dashboardService;

    public PlayDashboardController(IPlayDashboardService dashboardService)
    {
        this.dashboardService = dashboardService;
    }

    /** 查询玩法看板聚合数据。 */
    @Operation(summary = "查询玩法看板聚合数据")
    @PreAuthorize("@ss.hasPermi('bi:play:list')")
    @GetMapping
    public AjaxResult getDashboard(PlayDashboardQuery query)
    {
        return success(dashboardService.selectDashboard(query));
    }

    /** 导出玩法合规对账数据。 */
    @Operation(summary = "导出玩法合规对账数据")
    @Log(title = "玩法合规对账", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('bi:play:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PlayDashboardQuery query)
    {
        List<PlayGameOverviewVO> list = dashboardService.selectGameOverview(query);
        ExcelUtil<PlayGameOverviewVO> util = new ExcelUtil<>(PlayGameOverviewVO.class);
        util.exportExcel(response, list, "玩法合规对账");
    }

    /** 查询玩法看板可选地区。 */
    @Operation(summary = "查询玩法看板可选地区")
    @PreAuthorize("@ss.hasPermi('bi:play:list')")
    @GetMapping("/dimensions")
    public AjaxResult getDimensions(@RequestParam(required = false) Integer regionId)
    {
        return success(dashboardService.selectCountries(regionId));
    }
}
