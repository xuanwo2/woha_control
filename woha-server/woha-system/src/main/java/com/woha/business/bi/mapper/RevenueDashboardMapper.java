package com.woha.business.bi.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.woha.business.bi.domain.RevenueDashboardQuery;

/**
 * 营收看板数据访问层。
 *
 * @author luowenshen
 */
public interface RevenueDashboardMapper
{
    /** 查询营收汇总指标。 */
    Map<String, Object> selectSummary(@Param("query") RevenueDashboardQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 查询支付渠道分布。 */
    List<Map<String, Object>> selectPaymentChannels(@Param("query") RevenueDashboardQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 查询充值档位分布。 */
    List<Map<String, Object>> selectChargeTiers(@Param("query") RevenueDashboardQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 查询营收日趋势。 */
    List<Map<String, Object>> selectTrend(@Param("query") RevenueDashboardQuery query,
        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 查询可选地区集合。 */
    List<String> selectCountries(@Param("regionId") Integer regionId);
}
