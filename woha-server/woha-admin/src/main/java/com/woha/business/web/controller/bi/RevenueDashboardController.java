package com.woha.business.web.controller.bi;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.woha.business.bi.domain.RevenueDashboardQuery;
import com.woha.business.bi.service.IRevenueDashboardService;
import com.woha.business.common.core.controller.BaseController;
import com.woha.business.common.core.domain.AjaxResult;

/**
 * 营收看板接口。
 *
 * @author luowenshen
 */
@Tag(name = "营收看板")
@RestController
@RequestMapping("/bi/revenue")
public class RevenueDashboardController extends BaseController
{
    /** 营收看板服务 */
    private final IRevenueDashboardService dashboardService;

    public RevenueDashboardController(IRevenueDashboardService dashboardService)
    {
        this.dashboardService = dashboardService;
    }

    /** 查询营收看板聚合数据。 */
    @Operation(summary = "查询营收看板聚合数据")
    @PreAuthorize("@ss.hasPermi('bi:revenue:list')")
    @GetMapping
    public AjaxResult getDashboard(RevenueDashboardQuery query)
    {
        return success(dashboardService.selectDashboard(query));
    }

    /** 查询营收看板可选地区。 */
    @Operation(summary = "查询营收看板可选地区")
    @PreAuthorize("@ss.hasPermi('bi:revenue:list')")
    @GetMapping("/dimensions")
    public AjaxResult getDimensions(@RequestParam(required = false) Integer regionId)
    {
        return success(dashboardService.selectCountries(regionId));
    }
}
