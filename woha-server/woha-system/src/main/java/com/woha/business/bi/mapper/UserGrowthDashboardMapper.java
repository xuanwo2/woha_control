package com.woha.business.bi.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.woha.business.bi.domain.UserGrowthQuery;

/**
 * 用户增长看板数据访问层。
 *
 * @author luowenshen
 */
public interface UserGrowthDashboardMapper
{
    /**
     * 查询注册漏斗统计数据。
     *
     * @param query 查询条件
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 注册漏斗统计数据
     */
    Map<String, Object> selectFunnel(@Param("query") UserGrowthQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询注册同期群规模。
     *
     * @param query 查询条件
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 注册同期群规模集合
     */
    List<Map<String, Object>> selectCohortSizes(@Param("query") UserGrowthQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询注册同期群每日留存人数。
     *
     * @param query 查询条件
     * @param startTime 注册开始时间
     * @param endTime 注册结束时间
     * @param retentionEndTime 留存观察结束时间
     * @return 每日留存人数集合
     */
    List<Map<String, Object>> selectRetention(@Param("query") UserGrowthQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
        @Param("retentionEndTime") LocalDateTime retentionEndTime);

    /**
     * 查询登录辅助指标。
     *
     * @param query 查询条件
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录辅助指标
     */
    Map<String, Object> selectLoginSummary(@Param("query") UserGrowthQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询第三方登录渠道分布。
     *
     * @param query 查询条件
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 第三方登录渠道分布
     */
    List<Map<String, Object>> selectThirdLoginChannels(@Param("query") UserGrowthQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 查询可选地区集合。 */
    List<String> selectCountries(@Param("regionId") Integer regionId);

    /** 查询可选渠道集合。 */
    List<String> selectChannels(@Param("regionId") Integer regionId);

    /** 查询可选操作系统集合。 */
    List<String> selectOperatingSystems(@Param("regionId") Integer regionId);
}
