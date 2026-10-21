package com.woha.business.bi.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.woha.business.bi.domain.InviteGrowthDashboardVO;
import com.woha.business.bi.domain.InviteGrowthQuery;
import com.woha.business.bi.domain.InviteQualityVO;
import com.woha.business.bi.domain.UserGrowthDimensionVO;
import com.woha.business.bi.mapper.InviteGrowthDashboardMapper;
import com.woha.business.bi.service.IInviteGrowthDashboardService;
import com.woha.business.bi.service.DashboardRegionSupport;
import com.woha.business.common.annotation.DataSource;
import com.woha.business.common.enums.DataSourceType;
import com.woha.business.common.exception.ServiceException;

/**
 * 邀请增长看板服务实现。
 *
 * @author luowenshen
 */
@Service
@DataSource(DataSourceType.SLAVE)
public class InviteGrowthDashboardServiceImpl implements IInviteGrowthDashboardService
{
    /** 邀请汇总统计天数 */
    private static final int SUMMARY_DAYS = 7;

    /** 邀请趋势和质量分层统计天数 */
    private static final int ANALYSIS_DAYS = 30;

    /** 百分比计算基数 */
    private static final BigDecimal PERCENT_BASE = BigDecimal.valueOf(100);

    /** 百分比保留小数位数 */
    private static final int PERCENT_SCALE = 2;

    /** 疑似作弊的同设备账号阈值 */
    private static final long SUSPICIOUS_DEVICE_CLUSTER = 5L;

    /** 优质邀请人的最低次留率 */
    private static final BigDecimal PREMIUM_RETENTION_RATE = BigDecimal.valueOf(40);

    /** 优质邀请人的最低付费率 */
    private static final BigDecimal PREMIUM_PAY_RATE = BigDecimal.valueOf(5);

    /** 良好邀请人的最低次留率 */
    private static final BigDecimal GOOD_RETENTION_RATE = BigDecimal.valueOf(25);

    /** 日期格式化规则 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 日期时间格式化规则 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 邀请增长看板数据访问层 */
    private final InviteGrowthDashboardMapper dashboardMapper;

    public InviteGrowthDashboardServiceImpl(InviteGrowthDashboardMapper dashboardMapper)
    {
        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public InviteGrowthDashboardVO selectDashboard(InviteGrowthQuery query)
    {
        InviteGrowthQuery actualQuery = query == null ? new InviteGrowthQuery() : query;
        actualQuery.setRegionId(DashboardRegionSupport.resolve(actualQuery.getRegionId()));
        LocalDate anchorDate = parseDate(actualQuery.getDate());
        actualQuery.setDate(anchorDate.format(DATE_FORMATTER));
        LocalDate summaryStartDate = anchorDate.minusDays(SUMMARY_DAYS - 1L);
        LocalDate trendStartDate = anchorDate.minusDays(ANALYSIS_DAYS - 1L);
        LocalDateTime endTime = anchorDate.atTime(LocalTime.MAX);

        Map<String, Object> summary = dashboardMapper.selectSummary(actualQuery,
            anchorDate.atStartOfDay(), endTime, summaryStartDate.atStartOfDay(), endTime);
        long periodBindings = number(summary.get("periodBindings"));
        long activeInviters = number(summary.get("activeInviters"));

        InviteGrowthDashboardVO result = new InviteGrowthDashboardVO();
        result.setStartDate(summaryStartDate.format(DATE_FORMATTER));
        result.setEndDate(anchorDate.format(DATE_FORMATTER));
        result.setUpdateTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        result.setTodayBindings(number(summary.get("todayBindings")));
        result.setActiveInviters(activeInviters);
        result.setKFactor(ratio(periodBindings, activeInviters));
        result.setShareClickAvailable(false);
        result.setShareClicks(null);
        result.setInvitedD1Retention(percentage(number(summary.get("retainedUsers")),
            number(summary.get("retentionEligibleBindings"))));
        result.setBindingTrends(buildTrends(dashboardMapper.selectBindingTrend(actualQuery,
            trendStartDate.atStartOfDay(), endTime), trendStartDate, anchorDate));
        result.setFunnel(buildFunnel(activeInviters, periodBindings));
        return result;
    }

    @Override
    public List<InviteQualityVO> selectInviteQuality(InviteGrowthQuery query)
    {
        InviteGrowthQuery actualQuery = query == null ? new InviteGrowthQuery() : query;
        LocalDate anchorDate = parseDate(actualQuery.getDate());
        List<InviteQualityVO> result = dashboardMapper.selectInviteQuality(actualQuery,
            anchorDate.minusDays(ANALYSIS_DAYS - 1L).atStartOfDay(), anchorDate.atTime(LocalTime.MAX));
        for (InviteQualityVO item : result)
        {
            item.setInviter(maskUid(item.getInviterUid()));
            item.setD1Retention(decimal(item.getD1Retention()));
            item.setPayRate(decimal(item.getPayRate()));
            item.setGmv(decimal(item.getGmv()));
            item.setMaxDeviceCluster(item.getMaxDeviceCluster() == null ? 0L : item.getMaxDeviceCluster());
            fillDeviceStatus(item);
            fillQuality(item);
        }
        return result;
    }

    @Override
    public UserGrowthDimensionVO selectDimensions(Integer regionId)
    {
        Integer actualRegionId = DashboardRegionSupport.resolve(regionId);
        UserGrowthDimensionVO result = new UserGrowthDimensionVO();
        result.setCountries(dashboardMapper.selectCountries(actualRegionId));
        result.setChannels(dashboardMapper.selectChannels(actualRegionId));
        result.setOperatingSystems(dashboardMapper.selectOperatingSystems(actualRegionId));
        return result;
    }

    /** 解析查询日期，未传值时使用当前日期。 */
    private LocalDate parseDate(String date)
    {
        if (date == null || date.isBlank())
        {
            return LocalDate.now();
        }
        try
        {
            LocalDate parsedDate = LocalDate.parse(date, DATE_FORMATTER);
            if (parsedDate.isAfter(LocalDate.now()))
            {
                throw new ServiceException("查询日期不能晚于当前日期");
            }
            return parsedDate;
        }
        catch (DateTimeParseException exception)
        {
            throw new ServiceException("查询日期格式不正确，应为 yyyy-MM-dd");
        }
    }

    /** 填充连续三十日邀请绑定趋势。 */
    private List<InviteGrowthDashboardVO.TrendPoint> buildTrends(List<Map<String, Object>> rows,
        LocalDate startDate, LocalDate endDate)
    {
        Map<String, Long> values = new HashMap<>();
        for (Map<String, Object> row : rows)
        {
            values.put(String.valueOf(row.get("statDate")), number(row.get("bindings")));
        }
        List<InviteGrowthDashboardVO.TrendPoint> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1))
        {
            InviteGrowthDashboardVO.TrendPoint point = new InviteGrowthDashboardVO.TrendPoint();
            point.setDate(date.format(DATE_FORMATTER));
            point.setBindings(values.getOrDefault(point.getDate(), 0L));
            result.add(point);
        }
        return result;
    }

    /** 组装一期邀请漏斗，点击和安装环节保留待归因状态。 */
    private List<InviteGrowthDashboardVO.FunnelStage> buildFunnel(long activeInviters, long periodBindings)
    {
        List<InviteGrowthDashboardVO.FunnelStage> result = new ArrayList<>();
        result.add(funnelStage("有效邀请人数", "U14 分母", activeInviters, true));
        result.add(funnelStage("分享点击", "U15", null, false));
        result.add(funnelStage("安装", "归因环节", null, false));
        result.add(funnelStage("绑定成功", "U13", periodBindings, true));
        return result;
    }

    /** 创建邀请漏斗阶段。 */
    private InviteGrowthDashboardVO.FunnelStage funnelStage(String name, String metric, Long value,
        boolean available)
    {
        InviteGrowthDashboardVO.FunnelStage stage = new InviteGrowthDashboardVO.FunnelStage();
        stage.setName(name);
        stage.setMetric(metric);
        stage.setValue(value);
        stage.setAvailable(available);
        return stage;
    }

    /** 填充设备聚集状态。 */
    private void fillDeviceStatus(InviteQualityVO item)
    {
        long cluster = item.getMaxDeviceCluster() == null ? 0L : item.getMaxDeviceCluster();
        item.setDeviceStatus(cluster > 1 ? "同设备 " + cluster + " 人" : "正常");
    }

    /** 根据次留、付费和设备聚集情况填充质量等级。 */
    private void fillQuality(InviteQualityVO item)
    {
        BigDecimal retention = decimal(item.getD1Retention());
        BigDecimal payRate = decimal(item.getPayRate());
        long cluster = item.getMaxDeviceCluster() == null ? 0L : item.getMaxDeviceCluster();
        if (cluster >= SUSPICIOUS_DEVICE_CLUSTER)
        {
            item.setQualityCode("suspicious");
            item.setQualityName("疑似作弊");
        }
        else if (retention.compareTo(PREMIUM_RETENTION_RATE) >= 0
            && payRate.compareTo(PREMIUM_PAY_RATE) >= 0)
        {
            item.setQualityCode("premium");
            item.setQualityName("优质");
        }
        else if (retention.compareTo(GOOD_RETENTION_RATE) >= 0)
        {
            item.setQualityCode("good");
            item.setQualityName("良好");
        }
        else
        {
            item.setQualityCode("normal");
            item.setQualityName("普通");
        }
    }

    /** 对邀请人 UID 进行中间位脱敏。 */
    private String maskUid(Long uid)
    {
        if (uid == null)
        {
            return "--";
        }
        String value = String.valueOf(uid);
        if (value.length() <= 4)
        {
            return "U " + value.charAt(0) + "***";
        }
        int prefixLength = Math.min(4, value.length() - 2);
        return "U " + value.substring(0, prefixLength) + "****" + value.substring(value.length() - 2);
    }

    /** 计算百分比，分母为零时返回零。 */
    private BigDecimal percentage(long value, long baseValue)
    {
        if (baseValue <= 0)
        {
            return BigDecimal.ZERO.setScale(PERCENT_SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value).multiply(PERCENT_BASE)
            .divide(BigDecimal.valueOf(baseValue), PERCENT_SCALE, RoundingMode.HALF_UP);
    }

    /** 计算比例，分母为零时返回零。 */
    private BigDecimal ratio(long value, long baseValue)
    {
        if (baseValue <= 0)
        {
            return BigDecimal.ZERO.setScale(PERCENT_SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(baseValue), PERCENT_SCALE,
            RoundingMode.HALF_UP);
    }

    /** 将数据库数值转换为长整型。 */
    private long number(Object value)
    {
        return value == null ? 0L : new BigDecimal(String.valueOf(value)).longValue();
    }

    /** 将数值转换为非空高精度小数。 */
    private BigDecimal decimal(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}
