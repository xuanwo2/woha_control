package com.woha.business.web.controller.bi;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.woha.business.bi.domain.TrackingQualityIssue;
import com.woha.business.bi.domain.TrackingQualityIssueUpdate;
import com.woha.business.bi.service.ITrackingQualityService;
import com.woha.business.common.annotation.Log;
import com.woha.business.common.core.controller.BaseController;
import com.woha.business.common.core.domain.AjaxResult;
import com.woha.business.common.core.page.TableDataInfo;
import com.woha.business.common.enums.BusinessType;

/**
 * 埋点质量监控接口。
 *
 * @author luowenshen
 */
@Tag(name = "埋点质量监控")
@RestController
@RequestMapping("/bi/tracking-quality")
public class TrackingQualityController extends BaseController
{
    /** 埋点质量监控服务。 */
    private final ITrackingQualityService trackingQualityService;

    public TrackingQualityController(ITrackingQualityService trackingQualityService)
    {
        this.trackingQualityService = trackingQualityService;
    }

    /** 查询埋点质量监控看板。 */
    @Operation(summary = "查询埋点质量监控看板")
    @PreAuthorize("@ss.hasPermi('bi:trackingQuality:list')")
    @GetMapping
    public AjaxResult dashboard(@RequestParam(required = false) Integer regionId)
    {
        return success(trackingQualityService.selectDashboard(regionId));
    }

    /** 执行埋点质量检测并生成工单。 */
    @Operation(summary = "刷新埋点质量检测结果")
    @Log(title = "埋点质量检测刷新", businessType = BusinessType.OTHER)
    @PreAuthorize("@ss.hasPermi('bi:trackingQuality:refresh')")
    @PostMapping("/refresh")
    public AjaxResult refresh(@RequestParam(required = false) Integer regionId)
    {
        return success(trackingQualityService.refreshQualityIssues(regionId, getUsername()));
    }

    /** 查询埋点质量工单分页列表。 */
    @Operation(summary = "查询埋点质量工单分页列表")
    @PreAuthorize("@ss.hasPermi('bi:trackingQuality:list')")
    @GetMapping("/issue/list")
    public TableDataInfo issueList(TrackingQualityIssue query)
    {
        startPage();
        List<TrackingQualityIssue> list = trackingQualityService.selectIssueList(query);
        return getDataTable(list);
    }

    /** 更新埋点质量工单处理结果。 */
    @Operation(summary = "处理埋点质量工单")
    @Log(title = "埋点质量工单处理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bi:trackingQuality:manage')")
    @PutMapping("/issue/{issueId}")
    public AjaxResult updateIssue(@PathVariable Long issueId,
        @RequestParam(required = false) Integer regionId,
        @RequestBody TrackingQualityIssueUpdate update)
    {
        return toAjax(trackingQualityService.updateIssue(issueId, regionId, update, getUsername()));
    }
}
