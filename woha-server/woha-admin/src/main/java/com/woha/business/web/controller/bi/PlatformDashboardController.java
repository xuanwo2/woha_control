package com.woha.business.web.controller.bi;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.woha.business.bi.domain.PlatformDashboardQuery;
import com.woha.business.bi.service.IPlatformDashboardService;
import com.woha.business.common.core.controller.BaseController;
import com.woha.business.common.core.domain.AjaxResult;

/**
 * 平台大盘接口。
 *
 * @author luowenshen
 */
@Tag(name = "平台大盘")
@RestController
@RequestMapping("/bi/platform")
public class PlatformDashboardController extends BaseController
{
    /** 平台大盘服务 */
    private final IPlatformDashboardService dashboardService;

    public PlatformDashboardController(IPlatformDashboardService dashboardService)
    {
        this.dashboardService = dashboardService;
    }

    /**
     * 查询平台大盘聚合数据。
     */
    @Operation(summary = "查询平台大盘聚合数据")
    @PreAuthorize("@ss.hasPermi('bi:platform:list')")
    @GetMapping
    public AjaxResult getDashboard(PlatformDashboardQuery query)
    {
        return success(dashboardService.selectDashboard(query));
    }

    /**
     * 查询平台大盘筛选维度。
     */
    @Operation(summary = "查询平台大盘筛选维度")
    @PreAuthorize("@ss.hasPermi('bi:platform:list')")
    @GetMapping("/dimensions")
    public AjaxResult getDimensions(@RequestParam(required = false) Integer regionId)
    {
        return success(dashboardService.selectDimensions(regionId));
    }
}
