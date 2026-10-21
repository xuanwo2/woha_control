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
import com.woha.business.bi.domain.TrackingChangeRecord;
import com.woha.business.bi.domain.TrackingCommonParam;
import com.woha.business.bi.domain.TrackingEvent;
import com.woha.business.bi.domain.TrackingEventQuery;
import com.woha.business.bi.domain.TrackingReviewRequest;
import com.woha.business.bi.service.ITrackingDictionaryService;
import com.woha.business.common.annotation.Log;
import com.woha.business.common.core.controller.BaseController;
import com.woha.business.common.core.domain.AjaxResult;
import com.woha.business.common.core.page.TableDataInfo;
import com.woha.business.common.enums.BusinessType;

/**
 * 埋点字典平台接口。
 *
 * @author luowenshen
 */
@Tag(name = "埋点字典平台")
@RestController
@RequestMapping("/bi/tracking-dictionary")
public class TrackingDictionaryController extends BaseController
{
    /** 埋点字典服务。 */
    private final ITrackingDictionaryService trackingDictionaryService;

    public TrackingDictionaryController(ITrackingDictionaryService trackingDictionaryService)
    {
        this.trackingDictionaryService = trackingDictionaryService;
    }

    /** 查询事件字典分页列表。 */
    @Operation(summary = "查询埋点事件字典分页列表")
    @PreAuthorize("@ss.hasPermi('bi:tracking:list')")
    @GetMapping("/event/list")
    public TableDataInfo eventList(TrackingEventQuery query)
    {
        startPage();
        return getDataTable(trackingDictionaryService.selectEventList(query));
    }

    /** 查询事件详情。 */
    @Operation(summary = "查询埋点事件详情")
    @PreAuthorize("@ss.hasPermi('bi:tracking:list')")
    @GetMapping("/event/{eventId}")
    public AjaxResult eventDetail(@PathVariable Long eventId)
    {
        return success(trackingDictionaryService.selectEventById(eventId));
    }

    /** 查询业务模块选项。 */
    @Operation(summary = "查询埋点事件业务模块选项")
    @PreAuthorize("@ss.hasPermi('bi:tracking:list')")
    @GetMapping("/event/modules")
    public AjaxResult modules()
    {
        return success(trackingDictionaryService.selectModules());
    }

    /** 登记新事件。 */
    @Operation(summary = "登记新的埋点事件")
    @Log(title = "埋点事件登记", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('bi:tracking:add')")
    @PostMapping("/event")
    public AjaxResult addEvent(@RequestBody TrackingEvent event)
    {
        return toAjax(trackingDictionaryService.insertEvent(event, getUsername()));
    }

    /** 提交事件修改审批。 */
    @Operation(summary = "提交埋点事件修改审批")
    @Log(title = "埋点事件修改提审", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bi:tracking:edit')")
    @PutMapping("/event/submit")
    public AjaxResult submitEventChange(@RequestBody TrackingEvent event)
    {
        return toAjax(trackingDictionaryService.submitEventChange(event, getUsername()));
    }

    /** 更新事件状态。 */
    @Operation(summary = "更新埋点事件状态")
    @Log(title = "埋点事件状态", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bi:tracking:edit')")
    @PutMapping("/event/{eventId}/status")
    public AjaxResult updateEventStatus(@PathVariable Long eventId, @RequestParam String status)
    {
        return toAjax(trackingDictionaryService.updateEventStatus(eventId, status, getUsername()));
    }

    /** 查询公共参数分页列表。 */
    @Operation(summary = "查询埋点公共参数分页列表")
    @PreAuthorize("@ss.hasPermi('bi:tracking:list')")
    @GetMapping("/common-param/list")
    public TableDataInfo commonParamList(TrackingCommonParam param)
    {
        startPage();
        return getDataTable(trackingDictionaryService.selectCommonParamList(param));
    }

    /** 新增公共参数。 */
    @Operation(summary = "新增埋点公共参数")
    @Log(title = "埋点公共参数", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('bi:tracking:param')")
    @PostMapping("/common-param")
    public AjaxResult addCommonParam(@RequestBody TrackingCommonParam param)
    {
        return toAjax(trackingDictionaryService.insertCommonParam(param, getUsername()));
    }

    /** 更新公共参数。 */
    @Operation(summary = "更新埋点公共参数")
    @Log(title = "埋点公共参数", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bi:tracking:param')")
    @PutMapping("/common-param")
    public AjaxResult updateCommonParam(@RequestBody TrackingCommonParam param)
    {
        return toAjax(trackingDictionaryService.updateCommonParam(param, getUsername()));
    }

    /** 更新公共参数状态。 */
    @Operation(summary = "更新埋点公共参数状态")
    @Log(title = "埋点公共参数状态", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bi:tracking:param')")
    @PutMapping("/common-param/{commonParamId}/status")
    public AjaxResult updateCommonParamStatus(@PathVariable Long commonParamId,
        @RequestParam String status)
    {
        return toAjax(trackingDictionaryService.updateCommonParamStatus(commonParamId,
            status, getUsername()));
    }

    /** 查询变更审批分页列表。 */
    @Operation(summary = "查询埋点事件变更审批分页列表")
    @PreAuthorize("@ss.hasPermi('bi:tracking:list')")
    @GetMapping("/change/list")
    public TableDataInfo changeList(TrackingChangeRecord record)
    {
        startPage();
        List<TrackingChangeRecord> list = trackingDictionaryService.selectChangeList(record);
        return getDataTable(list);
    }

    /** 审批事件变更。 */
    @Operation(summary = "审批埋点事件变更")
    @Log(title = "埋点事件变更审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bi:tracking:review')")
    @PutMapping("/change/{changeId}/review")
    public AjaxResult reviewChange(@PathVariable Long changeId,
        @RequestBody TrackingReviewRequest request)
    {
        return toAjax(trackingDictionaryService.reviewChange(changeId, request, getUsername()));
    }
}
