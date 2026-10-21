package com.woha.business.web.controller.bi;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.woha.business.bi.domain.UserGrowthQuery;
import com.woha.business.bi.service.IUserGrowthDashboardService;
import com.woha.business.common.core.controller.BaseController;
import com.woha.business.common.core.domain.AjaxResult;

/**
 * 用户增长看板接口。
 *
 * @author luowenshen
 */
@Tag(name = "用户增长看板")
@RestController
@RequestMapping("/bi/user-growth")
public class UserGrowthDashboardController extends BaseController
{
    /** 用户增长看板服务 */
    private final IUserGrowthDashboardService dashboardService;

    public UserGrowthDashboardController(IUserGrowthDashboardService dashboardService)
    {
        this.dashboardService = dashboardService;
    }

    /**
     * 查询用户增长看板聚合数据。
     */
    @Operation(summary = "查询用户增长看板聚合数据")
    @PreAuthorize("@ss.hasPermi('bi:userGrowth:list')")
    @GetMapping
    public AjaxResult getDashboard(UserGrowthQuery query)
    {
        return success(dashboardService.selectDashboard(query));
    }

    /**
     * 查询用户增长看板筛选维度。
     */
    @Operation(summary = "查询用户增长看板筛选维度")
    @PreAuthorize("@ss.hasPermi('bi:userGrowth:list')")
    @GetMapping("/dimensions")
    public AjaxResult getDimensions(@RequestParam(required = false) Integer regionId)
    {
        return success(dashboardService.selectDimensions(regionId));
    }
}
