package com.woha.business.bi.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.woha.business.bi.domain.PlayDashboardQuery;

/**
 * 玩法看板数据访问层。
 *
 * @author luowenshen
 */
public interface PlayDashboardMapper
{
    /** 查询玩法总览数据。 */
    List<Map<String, Object>> selectGameOverview(@Param("query") PlayDashboardQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 查询砸蛋口径拆分数据。 */
    List<Map<String, Object>> selectLuckSegments(@Param("query") PlayDashboardQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 查询砸蛋漏斗中可计算的阶段。 */
    Map<String, Object> selectLuckFunnel(@Param("query") PlayDashboardQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 查询可选地区集合。 */
    List<String> selectCountries(@Param("regionId") Integer regionId);
}
