package com.woha.business.bi.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.woha.business.bi.domain.RevenueDashboardQuery;
import com.woha.business.bi.domain.RevenueDashboardVO;
import com.woha.business.bi.mapper.RevenueDashboardMapper;
import com.woha.business.bi.service.IRevenueDashboardService;
import com.woha.business.bi.service.DashboardRegionSupport;
import com.woha.business.common.annotation.DataSource;
import com.woha.business.common.enums.DataSourceType;
import com.woha.business.common.exception.ServiceException;

/**
 * 营收看板服务实现。
 *
 * @author luowenshen
 */
@Service
@DataSource(DataSourceType.SLAVE)
public class RevenueDashboardServiceImpl implements IRevenueDashboardService
{
    /** 默认查询最近七天 */
    private static final int DEFAULT_DAYS = 7;
    /** 单次查询允许的最大天数 */
    private static final int MAX_QUERY_DAYS = 366;
    /** 金币或钻石折算美元的固定汇率分母 */
    private static final BigDecimal COIN_USD_RATIO = BigDecimal.valueOf(10000);
    /** 百分比计算基数 */
    private static final BigDecimal PERCENT_BASE = BigDecimal.valueOf(100);
    /** 比例指标保留小数位数 */
    private static final int RATE_SCALE = 2;
    /** 金额指标保留小数位数 */
    private static final int MONEY_SCALE = 2;
    /** 日期格式化规则 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** 日期时间格式化规则 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 营收看板数据访问层 */
    private final RevenueDashboardMapper dashboardMapper;

    public RevenueDashboardServiceImpl(RevenueDashboardMapper dashboardMapper)
    {
        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public RevenueDashboardVO selectDashboard(RevenueDashboardQuery query)
    {
        RevenueDashboardQuery actualQuery = query == null ? new RevenueDashboardQuery() : query;
        actualQuery.setRegionId(DashboardRegionSupport.resolve(actualQuery.getRegionId()));
        LocalDate endDate = parseDate(actualQuery.getEndDate(), LocalDate.now(), "结束日期");
        LocalDate startDate = parseDate(actualQuery.getStartDate(), endDate.minusDays(DEFAULT_DAYS - 1L), "开始日期");
        validateRange(startDate, endDate);
        actualQuery.setStartDate(startDate.format(DATE_FORMATTER));
        actualQuery.setEndDate(endDate.format(DATE_FORMATTER));
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);
        Map<String, Object> summary = dashboardMapper.selectSummary(actualQuery, startTime, endTime);

        RevenueDashboardVO result = new RevenueDashboardVO();
        result.setStartDate(actualQuery.getStartDate());
        result.setEndDate(actualQuery.getEndDate());
        result.setUpdateTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        fillSummary(result, summary);
        result.setPaymentChannels(buildBreakdowns(dashboardMapper.selectPaymentChannels(actualQuery, startTime, endTime)));
        result.setChargeTiers(buildBreakdowns(dashboardMapper.selectChargeTiers(actualQuery, startTime, endTime)));
        result.setTrends(buildTrends(dashboardMapper.selectTrend(actualQuery, startTime, endTime), startDate, endDate));
        return result;
    }

    @Override
    public List<String> selectCountries(Integer regionId)
    {
        return dashboardMapper.selectCountries(DashboardRegionSupport.resolve(regionId));
    }

    /** 填充营收汇总指标和派生指标。 */
    private void fillSummary(RevenueDashboardVO result, Map<String, Object> data)
    {
        BigDecimal gmv = decimal(data.get("gmv"));
        BigDecimal trueRevenue = decimal(data.get("trueRevenueGold")).divide(COIN_USD_RATIO,
            MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal withdrawAmount = decimal(data.get("withdrawAmount"));
        long activeUsers = number(data.get("activeUsers"));
        long payUsers = number(data.get("payUsers"));
        long processedWithdrawals = number(data.get("processedWithdrawals"));
        long successfulWithdrawals = number(data.get("successfulWithdrawals"));

        result.setGmv(money(gmv));
        result.setTrueRevenue(trueRevenue);
        result.setWithdrawAmount(money(withdrawAmount));
        result.setNetFlow(money(gmv.subtract(withdrawAmount)));
        result.setActiveUsers(activeUsers);
        result.setPayUsers(payUsers);
        result.setPayRate(percentage(payUsers, activeUsers));
        result.setArpu(average(gmv, activeUsers));
        result.setArppu(average(gmv, payUsers));
        result.setFirstPayUsers(number(data.get("firstPayUsers")));
        result.setWithdrawApplications(number(data.get("withdrawApplications")));
        result.setProcessedWithdrawals(processedWithdrawals);
        result.setSuccessfulWithdrawals(successfulWithdrawals);
        result.setWithdrawSuccessRate(percentage(successfulWithdrawals, processedWithdrawals));
        result.setGiftGold(decimal(data.get("giftGold")));
        result.setGiftSendUsers(number(data.get("giftSendUsers")));
        result.setGiftReceiveUsers(number(data.get("giftReceiveUsers")));
        result.setVipBuyUsers(number(data.get("vipBuyUsers")));
        result.setVipBuyOrders(number(data.get("vipBuyOrders")));
        result.setVipAmount(money(decimal(data.get("vipAmount"))));
        result.setNobleBuyUsers(number(data.get("nobleBuyUsers")));
        result.setNobleBuyOrders(number(data.get("nobleBuyOrders")));
        result.setNobleGold(decimal(data.get("nobleGold")));
        result.setWalletGold(decimal(data.get("walletGold")));
        result.setWalletProxyGold(decimal(data.get("walletProxyGold")));
        result.setWalletDiamond(decimal(data.get("walletDiamond")));
        result.setPropBuyUsers(number(data.get("propBuyUsers")));
        result.setPropGold(decimal(data.get("propGold")));
    }

    /** 组装渠道和档位分布。 */
    private List<RevenueDashboardVO.BreakdownItem> buildBreakdowns(List<Map<String, Object>> rows)
    {
        BigDecimal total = rows.stream().map(row -> decimal(row.get("amount"))).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<RevenueDashboardVO.BreakdownItem> result = new ArrayList<>();
        for (Map<String, Object> row : rows)
        {
            RevenueDashboardVO.BreakdownItem item = new RevenueDashboardVO.BreakdownItem();
            item.setName(String.valueOf(row.get("name")));
            item.setAmount(money(decimal(row.get("amount"))));
            item.setOrders(number(row.get("orders")));
            item.setUsers(number(row.get("users")));
            item.setRatio(total.signum() == 0 ? BigDecimal.ZERO.setScale(RATE_SCALE)
                : item.getAmount().multiply(PERCENT_BASE).divide(total, RATE_SCALE, RoundingMode.HALF_UP));
            result.add(item);
        }
        return result;
    }

    /** 填充统计区间内连续日期的营收趋势。 */
    private List<RevenueDashboardVO.TrendPoint> buildTrends(List<Map<String, Object>> rows,
        LocalDate startDate, LocalDate endDate)
    {
        Map<String, Map<String, Object>> values = new HashMap<>();
        for (Map<String, Object> row : rows)
        {
            values.put(String.valueOf(row.get("statDate")), row);
        }
        List<RevenueDashboardVO.TrendPoint> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1))
        {
            Map<String, Object> row = values.getOrDefault(date.format(DATE_FORMATTER), Map.of());
            RevenueDashboardVO.TrendPoint point = new RevenueDashboardVO.TrendPoint();
            point.setDate(date.format(DATE_FORMATTER));
            point.setGmv(money(decimal(row.get("gmv"))));
            point.setTrueRevenue(decimal(row.get("trueRevenueGold")).divide(COIN_USD_RATIO,
                MONEY_SCALE, RoundingMode.HALF_UP));
            point.setWithdrawAmount(money(decimal(row.get("withdrawAmount"))));
            result.add(point);
        }
        return result;
    }

    /** 解析日期参数。 */
    private LocalDate parseDate(String value, LocalDate defaultValue, String fieldName)
    {
        if (value == null || value.isBlank())
        {
            return defaultValue;
        }
        try
        {
            return LocalDate.parse(value, DATE_FORMATTER);
        }
        catch (DateTimeParseException exception)
        {
            throw new ServiceException(fieldName + "格式不正确，应为 yyyy-MM-dd");
        }
    }

    /** 校验查询日期范围。 */
    private void validateRange(LocalDate startDate, LocalDate endDate)
    {
        if (endDate.isAfter(LocalDate.now()))
        {
            throw new ServiceException("结束日期不能晚于当前日期");
        }
        if (startDate.isAfter(endDate))
        {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) + 1 > MAX_QUERY_DAYS)
        {
            throw new ServiceException("单次查询范围不能超过 366 天");
        }
    }

    /** 计算百分比，分母为零时返回零。 */
    private BigDecimal percentage(long value, long baseValue)
    {
        if (baseValue <= 0)
        {
            return BigDecimal.ZERO.setScale(RATE_SCALE);
        }
        return BigDecimal.valueOf(value).multiply(PERCENT_BASE)
            .divide(BigDecimal.valueOf(baseValue), RATE_SCALE, RoundingMode.HALF_UP);
    }

    /** 计算人均金额，分母为零时返回零。 */
    private BigDecimal average(BigDecimal value, long users)
    {
        return users <= 0 ? BigDecimal.ZERO.setScale(MONEY_SCALE)
            : value.divide(BigDecimal.valueOf(users), MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /** 将数据库数值转换为长整型。 */
    private long number(Object value)
    {
        return decimal(value).longValue();
    }

    /** 将数据库数值转换为非空高精度小数。 */
    private BigDecimal decimal(Object value)
    {
        return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
    }

    /** 统一金额精度。 */
    private BigDecimal money(BigDecimal value)
    {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
