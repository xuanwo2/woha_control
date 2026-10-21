package com.woha.business.bi.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.woha.business.bi.domain.TrackingChangeRecord;
import com.woha.business.bi.domain.TrackingCommonParam;
import com.woha.business.bi.domain.TrackingEvent;
import com.woha.business.bi.domain.TrackingEventParam;
import com.woha.business.bi.domain.TrackingEventQuery;

/**
 * 埋点字典数据访问层。
 *
 * @author luowenshen
 */
public interface TrackingDictionaryMapper
{
    /** 查询事件字典列表。 */
    List<TrackingEvent> selectEventList(TrackingEventQuery query);

    /** 查询事件详情。 */
    TrackingEvent selectEventById(Long eventId);

    /** 查询事件参数。 */
    List<TrackingEventParam> selectEventParams(Long eventId);

    /** 查询已登记业务模块。 */
    List<String> selectModules();

    /** 统计重名事件。 */
    int countEventName(@Param("eventName") String eventName, @Param("eventId") Long eventId);

    /** 新增事件。 */
    int insertEvent(TrackingEvent event);

    /** 更新事件。 */
    int updateEvent(TrackingEvent event);

    /** 更新事件状态。 */
    int updateEventStatus(@Param("eventId") Long eventId, @Param("status") String status,
        @Param("updateBy") String updateBy);

    /** 删除事件参数，用于审批通过后重建参数快照。 */
    int deleteEventParams(Long eventId);

    /** 新增事件参数。 */
    int insertEventParam(TrackingEventParam param);

    /** 查询公共参数列表。 */
    List<TrackingCommonParam> selectCommonParamList(TrackingCommonParam param);

    /** 查询公共参数详情。 */
    TrackingCommonParam selectCommonParamById(Long commonParamId);

    /** 统计重名公共参数。 */
    int countCommonParamName(@Param("paramName") String paramName,
        @Param("commonParamId") Long commonParamId);

    /** 新增公共参数。 */
    int insertCommonParam(TrackingCommonParam param);

    /** 更新公共参数。 */
    int updateCommonParam(TrackingCommonParam param);

    /** 更新公共参数状态。 */
    int updateCommonParamStatus(@Param("commonParamId") Long commonParamId,
        @Param("status") String status, @Param("updateBy") String updateBy);

    /** 查询变更审批列表。 */
    List<TrackingChangeRecord> selectChangeList(TrackingChangeRecord record);

    /** 查询变更审批详情。 */
    TrackingChangeRecord selectChangeById(Long changeId);

    /** 统计事件待审批变更。 */
    int countPendingChange(Long eventId);

    /** 新增变更审批记录。 */
    int insertChangeRecord(TrackingChangeRecord record);

    /** 完成变更审批。 */
    int reviewChange(@Param("changeId") Long changeId,
        @Param("approvalStatus") String approvalStatus,
        @Param("approvalRemark") String approvalRemark, @Param("approveBy") String approveBy);
}
