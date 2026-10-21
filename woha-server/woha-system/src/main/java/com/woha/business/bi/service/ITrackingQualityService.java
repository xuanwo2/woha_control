package com.woha.business.bi.service;

import java.util.List;
import com.woha.business.bi.domain.TrackingQualityDashboardVO;
import com.woha.business.bi.domain.TrackingQualityIssue;
import com.woha.business.bi.domain.TrackingQualityIssueUpdate;

/**
 * 埋点质量监控服务。
 *
 * @author luowenshen
 */
public interface ITrackingQualityService
{
    /** 查询埋点质量监控看板。 */
    TrackingQualityDashboardVO selectDashboard(Integer regionId);

    /** 执行质量检测并生成工单。 */
    int refreshQualityIssues(Integer regionId, String operator);

    /** 查询质量工单列表。 */
    List<TrackingQualityIssue> selectIssueList(TrackingQualityIssue query);

    /** 更新质量工单处理结果。 */
    int updateIssue(Long issueId, Integer regionId, TrackingQualityIssueUpdate update, String operator);
}
