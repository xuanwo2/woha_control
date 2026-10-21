package com.woha.business.bi.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.woha.business.bi.domain.DashboardDimensionVO;
import com.woha.business.bi.domain.PlatformDashboardQuery;
import com.woha.business.bi.domain.PlatformDashboardVO;
import com.woha.business.bi.mapper.PlatformDashboardMapper;
import com.woha.business.bi.service.IPlatformDashboardService;
import com.woha.business.bi.service.DashboardRegionSupport;
import com.woha.business.common.annotation.DataSource;
import com.woha.business.common.core.redis.RedisCache;
import com.woha.business.common.enums.DataSourceType;
import com.woha.business.common.exception.ServiceException;

/**
 * 平台大盘服务实现。
 *
 * @author luowenshen
 */
@Service
@DataSource(DataSourceType.SLAVE)
public class PlatformDashboardServiceImpl implements IPlatformDashboardService
{
    /** 实时粒度业务编码 */
    private static final String PERIOD_REALTIME = "realtime";

    /** 日粒度业务编码 */
    private static final String PERIOD_DAY = "day";

    /** 周粒度业务编码 */
    private static final String PERIOD_WEEK = "week";

    /** 月粒度业务编码 */
    private static final String PERIOD_MONTH = "month";

    /** 日期格式化规则 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 日期时间格式化规则 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 百分比计算基数 */
    private static final BigDecimal PERCENT_BASE = BigDecimal.valueOf(100);

    /** 平台大盘数据访问层 */
    private final PlatformDashboardMapper dashboardMapper;

    /** 平台大盘筛选维度缓存有效期，单位：分钟 */
    private static final int DIMENSION_CACHE_MINUTES = 5;

    /** 平台大盘筛选维度缓存键前缀 */
    private static final String DIMENSION_CACHE_KEY_PREFIX = "bi:platform:dimensions:";

    /** 平台大盘缓存键前缀 */
    private static final String DASHBOARD_CACHE_KEY_PREFIX = "bi:platform:dashboard:";

    /** 实时大盘缓存有效期，单位：秒 */
    private static final int REALTIME_DASHBOARD_CACHE_SECONDS = 60;

    /** 历史大盘缓存有效期，单位：分钟 */
    private static final int HISTORY_DASHBOARD_CACHE_MINUTES = 5;

    /** Redis 缓存工具 */
    private final RedisCache redisCache;

    /** 异常波动阈值，默认相对滚动基线波动百分之三十 */
    @Value("${bi.dashboard.abnormal-rate:0.30}")
    private BigDecimal abnormalRate;

    public PlatformDashboardServiceImpl(PlatformDashboardMapper dashboardMapper, RedisCache redisCache)
    {
        this.dashboardMapper = dashboardMapper;
        this.redisCache = redisCache;
    }

    @Override
    public PlatformDashboardVO selectDashboard(PlatformDashboardQuery query)
    {
        PlatformDashboardQuery actualQuery = query == null ? new PlatformDashboardQuery() : query;
        actualQuery.setRegionId(DashboardRegionSupport.resolve(actualQuery.getRegionId()));
        LocalDate anchorDate = parseDate(actualQuery.getDate());
        String period = normalizePeriod(actualQuery.getPeriod());
        actualQuery.setPeriod(period);
        actualQuery.setDate(anchorDate.format(DATE_FORMATTER));

        LocalDateTime[] range = resolveRange(anchorDate, period);
        String cacheKey = buildDashboardCacheKey(actualQuery);
        PlatformDashboardVO cachedResult = redisCache.getCacheObject(cacheKey);
        if (cachedResult != null)
        {
            return cachedResult;
        }

        PlatformDashboardVO result = buildDashboard(actualQuery, range, anchorDate);
        if (PERIOD_REALTIME.equals(period) && anchorDate.equals(LocalDate.now()))
        {
            redisCache.setCacheObject(cacheKey, result, REALTIME_DASHBOARD_CACHE_SECONDS, TimeUnit.SECONDS);
        }
        else
        {
            redisCache.setCacheObject(cacheKey, result, HISTORY_DASHBOARD_CACHE_MINUTES, TimeUnit.MINUTES);
        }
        return result;
    }

    /**
     * 查询并组装未命中缓存的平台大盘数据。
     */
    private PlatformDashboardVO buildDashboard(PlatformDashboardQuery query, LocalDateTime[] range, LocalDate anchorDate)
    {
        Map<String, Object> kpiData = dashboardMapper.selectKpi(query, range[0], range[1]);
        LocalDate trendStartDate = anchorDate.minusDays(29);
        List<Map<String, Object>> trendRows = dashboardMapper.selectDailyTrend(query,
            trendStartDate.atStartOfDay(), anchorDate.atTime(LocalTime.MAX));
        LocalDate settledStartDate = range[0].toLocalDate().minusDays(1);
        LocalDate settledEndDate = range[1].toLocalDate().minusDays(1);
        LocalDate settledTrendStartDate = trendStartDate.minusDays(1).isBefore(settledStartDate)
            ? trendStartDate.minusDays(1) : settledStartDate;
        LocalDate settledTrendEndDate = anchorDate.minusDays(1).isAfter(settledEndDate)
            ? anchorDate.minusDays(1) : settledEndDate;
        List<Map<String, Object>> settledTrendRows = dashboardMapper.selectSettledRevenueTrend(query,
            settledTrendStartDate.format(DATE_FORMATTER), settledTrendEndDate.format(DATE_FORMATTER));
        Map<String, Object> settledRevenue = dashboardMapper.selectSettledRevenue(query,
            settledStartDate.format(DATE_FORMATTER), settledEndDate.format(DATE_FORMATTER));
        fillSettledRevenue(settledRevenue, settledTrendRows, settledStartDate, settledEndDate);

        PlatformDashboardVO result = new PlatformDashboardVO();
        result.setStartTime(range[0].format(DATE_TIME_FORMATTER));
        result.setEndTime(range[1].format(DATE_TIME_FORMATTER));
        result.setUpdateTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        result.setTrends(buildTrends(trendRows, settledTrendRows, trendStartDate, anchorDate));
        result.setKpis(buildKpis(kpiData, settledRevenue, settledTrendRows, result.getTrends()));
        result.setOnlineTrends(buildOnlineTrends(dashboardMapper.selectOnlineTrend(query)));
        result.setRegionRanks(buildRegionRanks(dashboardMapper.selectRegionRank(query, range[0], range[1])));
        return result;
    }

    /**
     * 从已查询的日结趋势中汇总营收和毛利，避免重复扫描充值、提现流水。
     */
    private void fillSettledRevenue(Map<String, Object> settledRevenue, List<Map<String, Object>> rows,
        LocalDate startDate, LocalDate endDate)
    {
        BigDecimal netRevenue = BigDecimal.ZERO;
        BigDecimal grossProfit = BigDecimal.ZERO;
        for (Map<String, Object> row : rows)
        {
            LocalDate statDate = LocalDate.parse(String.valueOf(row.get("statDate")), DATE_FORMATTER);
            if (!statDate.isBefore(startDate) && !statDate.isAfter(endDate))
            {
                netRevenue = netRevenue.add(decimal(row.get("netRevenue")));
                grossProfit = grossProfit.add(decimal(row.get("grossProfit")));
            }
        }
        settledRevenue.put("netRevenue", netRevenue);
        settledRevenue.put("grossProfit", grossProfit);
    }

    /**
     * 根据规范化后的查询条件生成平台大盘缓存键。
     */
    private String buildDashboardCacheKey(PlatformDashboardQuery query)
    {
        return DASHBOARD_CACHE_KEY_PREFIX + query.getRegionId() + ":period:" + query.getPeriod()
            + ":date:" + query.getDate() + ":country:" + String.valueOf(query.getCountry())
            + ":channel:" + String.valueOf(query.getChannel());
    }

    @Override
    public DashboardDimensionVO selectDimensions(Integer regionId)
    {
        Integer actualRegionId = DashboardRegionSupport.resolve(regionId);
        String cacheKey = DIMENSION_CACHE_KEY_PREFIX + actualRegionId;
        DashboardDimensionVO cachedResult = redisCache.getCacheObject(cacheKey);
        if (cachedResult != null)
        {
            return copyDimensions(cachedResult);
        }
        DashboardDimensionVO result = new DashboardDimensionVO();
        result.setCountries(dashboardMapper.selectCountries(actualRegionId));
        result.setChannels(dashboardMapper.selectChannels(actualRegionId));
        redisCache.setCacheObject(cacheKey, result, DIMENSION_CACHE_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    /**
     * 复制筛选维度，避免调用方修改缓存对象。
     */
    private DashboardDimensionVO copyDimensions(DashboardDimensionVO source)
    {
        DashboardDimensionVO result = new DashboardDimensionVO();
        result.setCountries(new ArrayList<>(source.getCountries()));
        result.setChannels(new ArrayList<>(source.getChannels()));
        return result;
    }

    /**
     * 解析查询日期，未传值时使用当前日期。
     */
    private LocalDate parseDate(String date)
    {
        if (date == null || date.isBlank())
        {
            return LocalDate.now();
        }
        try
        {
            return LocalDate.parse(date, DATE_FORMATTER);
        }
        catch (DateTimeParseException exception)
        {
            throw new ServiceException("查询日期格式不正确，应为 yyyy-MM-dd");
        }
    }

    /**
     * 规范时间粒度。
     */
    private String normalizePeriod(String period)
    {
        if (period == null || period.isBlank())
        {
            return PERIOD_REALTIME;
        }
        if (PERIOD_REALTIME.equals(period) || PERIOD_DAY.equals(period)
            || PERIOD_WEEK.equals(period) || PERIOD_MONTH.equals(period))
        {
            return period;
        }
        throw new ServiceException("时间粒度仅支持 realtime、day、week、month");
    }

    /**
     * 根据时间粒度计算统计区间。
     */
    private LocalDateTime[] resolveRange(LocalDate anchorDate, String period)
    {
        LocalDate startDate = anchorDate;
        LocalDate endDate = anchorDate;
        if (PERIOD_WEEK.equals(period))
        {
            startDate = anchorDate.with(DayOfWeek.MONDAY);
            endDate = startDate.plusDays(6);
        }
        else if (PERIOD_MONTH.equals(period))
        {
            startDate = anchorDate.withDayOfMonth(1);
            endDate = anchorDate.withDayOfMonth(anchorDate.lengthOfMonth());
        }
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);
        if (PERIOD_REALTIME.equals(period) && anchorDate.equals(LocalDate.now()))
        {
            endTime = LocalDateTime.now();
        }
        return new LocalDateTime[] { startDate.atStartOfDay(), endTime };
    }

    /**
     * 组装 KPI 指标集合。
     */
    private List<PlatformDashboardVO.KpiItem> buildKpis(Map<String, Object> data, Map<String, Object> settledRevenue,
        List<Map<String, Object>> settledTrendRows, List<PlatformDashboardVO.TrendPoint> trends)
    {
        BigDecimal dau = decimal(data.get("dau"));
        BigDecimal dnu = decimal(data.get("dnu"));
        BigDecimal gmv = decimal(data.get("gmv"));
        BigDecimal giftGold = decimal(data.get("giftGold"));
        BigDecimal payUsers = decimal(settledRevenue.get("settledPayUsers"));
        BigDecimal netRevenue = decimal(data.get("netRevenue"));
        BigDecimal grossProfit = decimal(settledRevenue.get("grossProfit"));
        BigDecimal settledDau = decimal(settledRevenue.get("settledDau"));
        BigDecimal payRate = settledDau.signum() == 0 ? BigDecimal.ZERO
            : payUsers.multiply(PERCENT_BASE).divide(settledDau, 2, RoundingMode.HALF_UP);

        BigDecimal revenueBaseline = averageSettledRevenue(settledTrendRows, 3);
        BigDecimal profitBaseline = average(trends, 7, true);
        List<PlatformDashboardVO.KpiItem> result = new ArrayList<>();
        result.add(kpi("dau", "DAU", "U2", dau, "人", "实时≈", false, BigDecimal.ZERO));
        result.add(kpi("dnu", "新增 DNU", "U1", dnu, "人", "实时≈", false, BigDecimal.ZERO));
        result.add(kpi("gmv", "总充值 GMV", "R1", gmv, "USD", "实时", false, BigDecimal.ZERO));
        result.add(kpi("netRevenue", "真营收", "R22", netRevenue, "USD", "实时",
            isAbnormal(netRevenue, revenueBaseline), changeRate(netRevenue, revenueBaseline)));
        result.add(kpi("grossProfit", "平台毛利", "R23", grossProfit, "USD", "T+1",
            isAbnormal(grossProfit, profitBaseline), changeRate(grossProfit, profitBaseline)));
        result.add(kpi("giftFlow", "礼物流水", "R11", giftGold, "金币", "实时", false, BigDecimal.ZERO));
        result.add(kpi("payRate", "付费率", "R5", payRate, "%", "T+1", false, BigDecimal.ZERO));
        result.add(kpi("online", "在线房间/人数", "P1/P2", decimal(data.get("onlineRooms")),
            decimal(data.get("onlineUsers")).stripTrailingZeros().toPlainString() + " 人", "实时", false, BigDecimal.ZERO));
        return result;
    }

    /**
     * 计算已日结真营收滚动均值。
     */
    private BigDecimal averageSettledRevenue(List<Map<String, Object>> rows, int size)
    {
        if (rows.isEmpty())
        {
            return BigDecimal.ZERO;
        }
        int startIndex = Math.max(0, rows.size() - size);
        BigDecimal total = BigDecimal.ZERO;
        for (int index = startIndex; index < rows.size(); index++)
        {
            total = total.add(decimal(rows.get(index).get("netRevenue")));
        }
        return total.divide(BigDecimal.valueOf(rows.size() - startIndex), 2, RoundingMode.HALF_UP);
    }

    /**
     * 创建 KPI 指标项。
     */
    private PlatformDashboardVO.KpiItem kpi(String code, String name, String metric, BigDecimal value,
        String unit, String freshness, boolean abnormal, BigDecimal changeRate)
    {
        PlatformDashboardVO.KpiItem item = new PlatformDashboardVO.KpiItem();
        item.setCode(code);
        item.setName(name);
        item.setMetric(metric);
        item.setValue(value);
        item.setUnit(unit);
        item.setFreshness(freshness);
        item.setAbnormal(abnormal);
        item.setChangeRate(changeRate);
        return item;
    }

    /**
     * 填充连续 30 日趋势数据。
     */
    private List<PlatformDashboardVO.TrendPoint> buildTrends(List<Map<String, Object>> rows,
        List<Map<String, Object>> settledRevenueRows,
        LocalDate startDate, LocalDate endDate)
    {
        Map<String, Map<String, Object>> rowMap = new HashMap<>();
        for (Map<String, Object> row : rows)
        {
            rowMap.put(String.valueOf(row.get("statDate")), row);
        }
        Map<String, Map<String, Object>> settledRevenueMap = new HashMap<>();
        for (Map<String, Object> row : settledRevenueRows)
        {
            settledRevenueMap.put(String.valueOf(row.get("statDate")), row);
        }
        List<PlatformDashboardVO.TrendPoint> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1))
        {
            Map<String, Object> row = rowMap.getOrDefault(date.toString(), Map.of());
            Map<String, Object> settledRevenue = settledRevenueMap.getOrDefault(date.minusDays(1).toString(), Map.of());
            PlatformDashboardVO.TrendPoint point = new PlatformDashboardVO.TrendPoint();
            point.setDate(date.toString());
            point.setGmv(decimal(row.get("gmv")));
            point.setDau(decimal(row.get("dau")).longValue());
            point.setGrossProfit(decimal(settledRevenue.get("grossProfit")));
            result.add(point);
        }
        return result;
    }

    /**
     * 组装在线趋势数据。
     */
    private List<PlatformDashboardVO.OnlinePoint> buildOnlineTrends(List<Map<String, Object>> rows)
    {
        List<PlatformDashboardVO.OnlinePoint> result = new ArrayList<>();
        for (Map<String, Object> row : rows)
        {
            PlatformDashboardVO.OnlinePoint point = new PlatformDashboardVO.OnlinePoint();
            point.setTime(String.valueOf(row.get("statTime")));
            point.setOnlineUsers(decimal(row.get("onlineUsers")).longValue());
            result.add(point);
        }
        return result;
    }

    /**
     * 组装地区充值排名。
     */
    private List<PlatformDashboardVO.RegionRank> buildRegionRanks(List<Map<String, Object>> rows)
    {
        List<PlatformDashboardVO.RegionRank> result = new ArrayList<>();
        for (Map<String, Object> row : rows)
        {
            PlatformDashboardVO.RegionRank rank = new PlatformDashboardVO.RegionRank();
            rank.setCountry(String.valueOf(row.get("country")));
            rank.setAmount(decimal(row.get("amount")));
            rank.setPayUsers(decimal(row.get("payUsers")).longValue());
            result.add(rank);
        }
        return result;
    }

    /**
     * 计算最近若干日滚动基线，排除趋势最后一天。
     */
    private BigDecimal average(List<PlatformDashboardVO.TrendPoint> trends, int days, boolean profit)
    {
        int endIndex = Math.max(0, trends.size() - 1);
        int startIndex = Math.max(0, endIndex - days);
        if (startIndex == endIndex)
        {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (int index = startIndex; index < endIndex; index++)
        {
            total = total.add(profit ? trends.get(index).getGrossProfit() : trends.get(index).getGmv());
        }
        return total.divide(BigDecimal.valueOf(endIndex - startIndex), 4, RoundingMode.HALF_UP);
    }

    /**
     * 判断指标是否超出滚动基线阈值。
     */
    private boolean isAbnormal(BigDecimal value, BigDecimal baseline)
    {
        return baseline.signum() > 0 && changeRate(value, baseline).abs().compareTo(abnormalRate) >= 0;
    }

    /**
     * 计算指标相对基线变化比例。
     */
    private BigDecimal changeRate(BigDecimal value, BigDecimal baseline)
    {
        if (baseline.signum() == 0)
        {
            return BigDecimal.ZERO;
        }
        return value.subtract(baseline).divide(baseline, 4, RoundingMode.HALF_UP);
    }

    /**
     * 将数据库数值安全转换为 BigDecimal。
     */
    private BigDecimal decimal(Object value)
    {
        return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
    }
}
