package com.woha.business.bi.service;

import java.util.List;
import java.util.Map;
import com.woha.business.bi.domain.TrackingAppRelease;
import com.woha.business.bi.domain.TrackingQualitySourceEventStat;

/**
 * 业务库埋点质量数据服务。
 *
 * @author luowenshen
 */
public interface ITrackingQualitySourceService
{
    /** 查询今日成功上报总量。 */
    Long selectTodayReports(Integer regionId);

    /** 查询近二十四小时成功与拒绝总量。 */
    Map<String, Object> selectRecentTotals(Integer regionId);

    /** 查询事件质量聚合数据。 */
    List<TrackingQualitySourceEventStat> selectEventStats(Integer regionId);

    /** 查询最近发布的 App 版本。 */
    List<TrackingAppRelease> selectRecentReleases();

    /** 查询指定 App 版本实际出现的事件。 */
    List<String> selectVersionEvents(String versionNo, Integer regionId);
}
