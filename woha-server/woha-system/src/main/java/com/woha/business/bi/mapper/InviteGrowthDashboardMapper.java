package com.woha.business.bi.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.woha.business.bi.domain.InviteGrowthQuery;
import com.woha.business.bi.domain.InviteQualityVO;

/**
 * 邀请增长看板数据访问层。
 *
 * @author luowenshen
 */
public interface InviteGrowthDashboardMapper
{
    /** 查询邀请增长汇总指标。 */
    Map<String, Object> selectSummary(@Param("query") InviteGrowthQuery query,
        @Param("todayStartTime") LocalDateTime todayStartTime,
        @Param("todayEndTime") LocalDateTime todayEndTime,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);

    /** 查询最近三十日邀请绑定趋势。 */
    List<Map<String, Object>> selectBindingTrend(@Param("query") InviteGrowthQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 查询邀请人质量分层数据。 */
    List<InviteQualityVO> selectInviteQuality(@Param("query") InviteGrowthQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 查询可选地区集合。 */
    List<String> selectCountries(@Param("regionId") Integer regionId);

    /** 查询可选渠道集合。 */
    List<String> selectChannels(@Param("regionId") Integer regionId);

    /** 查询可选操作系统集合。 */
    List<String> selectOperatingSystems(@Param("regionId") Integer regionId);
}
