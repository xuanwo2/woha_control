package com.woha.business.bi.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.woha.business.bi.domain.TrackingAppRelease;
import com.woha.business.bi.domain.TrackingQualitySourceEventStat;

/**
 * 业务库埋点质量数据访问层。
 *
 * @author luowenshen
 */
public interface TrackingQualitySourceMapper
{
    /** 查询今日成功上报总量。 */
    Long selectTodayReports(@Param("regionId") Integer regionId);

    /** 查询近二十四小时成功与拒绝总量。 */
    Map<String, Object> selectRecentTotals(@Param("regionId") Integer regionId);

    /** 查询近四十八小时事件质量聚合数据。 */
    List<TrackingQualitySourceEventStat> selectEventStats(@Param("regionId") Integer regionId);

    /** 查询最近发布的 App 版本。 */
    List<TrackingAppRelease> selectRecentReleases();

    /** 查询指定 App 版本实际出现的事件。 */
    List<String> selectVersionEvents(@Param("versionNo") String versionNo,
        @Param("regionId") Integer regionId);
}
