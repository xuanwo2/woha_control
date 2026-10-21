package com.woha.business.bi.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.woha.business.bi.domain.TrackingEvent;
import com.woha.business.bi.domain.TrackingQualityIssue;
import com.woha.business.bi.domain.TrackingQualityIssueUpdate;

/**
 * 埋点质量监控主库数据访问层。
 *
 * @author luowenshen
 */
public interface TrackingQualityMapper
{
    /** 查询启用的客户端埋点事件。 */
    List<TrackingEvent> selectEnabledClientEvents();

    /** 查询全部启用的埋点事件。 */
    List<TrackingEvent> selectEnabledEvents();

    /** 查询版本发布时间区间内审批通过的事件。 */
    List<String> selectExpectedReleaseEvents(@Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);

    /** 查询质量工单列表。 */
    List<TrackingQualityIssue> selectIssueList(TrackingQualityIssue query);

    /** 查询质量工单详情。 */
    TrackingQualityIssue selectIssueById(@Param("issueId") Long issueId,
        @Param("regionId") Integer regionId);

    /** 查询尚未完成的质量工单。 */
    List<TrackingQualityIssue> selectActiveIssues(@Param("regionId") Integer regionId);

    /** 统计待处理质量工单。 */
    Long countPendingIssues(@Param("regionId") Integer regionId);

    /** 新增或更新自动检测质量工单。 */
    int upsertIssue(TrackingQualityIssue issue);

    /** 更新质量工单处理结果。 */
    int updateIssue(@Param("issueId") Long issueId,
        @Param("regionId") Integer regionId,
        @Param("update") TrackingQualityIssueUpdate update,
        @Param("updateBy") String updateBy);
}
