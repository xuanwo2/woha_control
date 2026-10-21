package com.woha.business.bi.service;

import java.util.List;
import com.woha.business.bi.domain.TrackingChangeRecord;
import com.woha.business.bi.domain.TrackingCommonParam;
import com.woha.business.bi.domain.TrackingEvent;
import com.woha.business.bi.domain.TrackingEventQuery;
import com.woha.business.bi.domain.TrackingReviewRequest;

/**
 * 埋点字典服务。
 *
 * @author luowenshen
 */
public interface ITrackingDictionaryService
{
    /** 查询事件字典列表。 */
    List<TrackingEvent> selectEventList(TrackingEventQuery query);

    /** 查询事件详情。 */
    TrackingEvent selectEventById(Long eventId);

    /** 查询已登记业务模块。 */
    List<String> selectModules();

    /** 登记新事件。 */
    int insertEvent(TrackingEvent event, String operator);

    /** 提交事件修改审批。 */
    int submitEventChange(TrackingEvent event, String operator);

    /** 更新事件状态。 */
    int updateEventStatus(Long eventId, String status, String operator);

    /** 查询公共参数列表。 */
    List<TrackingCommonParam> selectCommonParamList(TrackingCommonParam param);

    /** 新增公共参数。 */
    int insertCommonParam(TrackingCommonParam param, String operator);

    /** 更新公共参数。 */
    int updateCommonParam(TrackingCommonParam param, String operator);

    /** 更新公共参数状态。 */
    int updateCommonParamStatus(Long commonParamId, String status, String operator);

    /** 查询变更审批列表。 */
    List<TrackingChangeRecord> selectChangeList(TrackingChangeRecord record);

    /** 审批事件变更。 */
    int reviewChange(Long changeId, TrackingReviewRequest request, String operator);
}
