package com.woha.business.bi.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.woha.business.bi.domain.PlatformDashboardQuery;

/**
 * 平台大盘数据访问层。
 *
 * @author luowenshen
 */
public interface PlatformDashboardMapper
{
    /**
     * 查询平台 KPI 原始指标。
     *
     * @param query 查询条件
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @return KPI 原始指标
     */
    Map<String, Object> selectKpi(@Param("query") PlatformDashboardQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询已日结营收及平台毛利数据。
     *
     * @param query 查询条件
     * @param startDate 日结开始日期
     * @param endDate 日结结束日期
     * @return 已日结营收指标
     */
    Map<String, Object> selectSettledRevenue(@Param("query") PlatformDashboardQuery query,
        @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 查询已日结营收趋势数据。
     *
     * @param query 查询条件
     * @param startDate 日结开始日期
     * @param endDate 日结结束日期
     * @return 已日结营收趋势
     */
    List<Map<String, Object>> selectSettledRevenueTrend(@Param("query") PlatformDashboardQuery query,
        @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 查询近 30 日趋势。
     *
     * @param query 查询条件
     * @param startTime 趋势开始时间
     * @param endTime 趋势结束时间
     * @return 每日趋势数据
     */
    List<Map<String, Object>> selectDailyTrend(@Param("query") PlatformDashboardQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最近一小时在线趋势。
     *
     * @param query 查询条件
     * @return 在线趋势数据
     */
    List<Map<String, Object>> selectOnlineTrend(@Param("query") PlatformDashboardQuery query);

    /**
     * 查询地区充值排名。
     *
     * @param query 查询条件
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @return 地区充值排名
     */
    List<Map<String, Object>> selectRegionRank(@Param("query") PlatformDashboardQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询地区筛选项。
     *
     * @return 地区国家编码列表
     */
    List<String> selectCountries(@Param("regionId") Integer regionId);

    /**
     * 查询渠道筛选项。
     *
     * @return 用户来源渠道列表
     */
    List<String> selectChannels(@Param("regionId") Integer regionId);
}
