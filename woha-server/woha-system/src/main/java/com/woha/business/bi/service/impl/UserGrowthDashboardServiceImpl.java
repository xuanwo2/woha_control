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
import com.woha.business.bi.domain.UserGrowthDashboardVO;
import com.woha.business.bi.domain.UserGrowthDimensionVO;
import com.woha.business.bi.domain.UserGrowthQuery;
import com.woha.business.bi.mapper.UserGrowthDashboardMapper;
import com.woha.business.bi.service.IUserGrowthDashboardService;
import com.woha.business.bi.service.DashboardRegionSupport;
import com.woha.business.common.annotation.DataSource;
import com.woha.business.common.enums.DataSourceType;
import com.woha.business.common.exception.ServiceException;

/**
 * 用户增长看板服务实现。
 *
 * @author luowenshen
 */
@Service
@DataSource(DataSourceType.SLAVE)
public class UserGrowthDashboardServiceImpl implements IUserGrowthDashboardService
{
    /** 注册漏斗统计天数 */
    private static final int FUNNEL_DAYS = 7;

    /** 留存曲线最大观察天数 */
    private static final int MAX_RETENTION_DAY = 30;

    /** 留存矩阵展示天数 */
    private static final int MATRIX_DAYS = 7;

    /** 百分比计算基数 */
    private static final BigDecimal PERCENT_BASE = BigDecimal.valueOf(100);

    /** 百分比保留小数位数 */
    private static final int PERCENT_SCALE = 2;

    /** 日期格式化规则 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 日期时间格式化规则 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 用户增长看板数据访问层 */
    private final UserGrowthDashboardMapper dashboardMapper;

    public UserGrowthDashboardServiceImpl(UserGrowthDashboardMapper dashboardMapper)
    {
        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public UserGrowthDashboardVO selectDashboard(UserGrowthQuery query)
    {
        UserGrowthQuery actualQuery = query == null ? new UserGrowthQuery() : query;
        actualQuery.setRegionId(DashboardRegionSupport.resolve(actualQuery.getRegionId()));
        LocalDate anchorDate = parseDate(actualQuery.getDate());
        actualQuery.setDate(anchorDate.format(DATE_FORMATTER));

        LocalDate funnelStartDate = anchorDate.minusDays(FUNNEL_DAYS - 1L);
        LocalDate cohortStartDate = anchorDate.minusDays(MAX_RETENTION_DAY);
        LocalDateTime funnelStartTime = funnelStartDate.atStartOfDay();
        LocalDateTime endTime = anchorDate.atTime(LocalTime.MAX);
        LocalDateTime cohortStartTime = cohortStartDate.atStartOfDay();

        Map<String, Object> funnelData = dashboardMapper.selectFunnel(actualQuery, funnelStartTime, endTime);
        List<Map<String, Object>> cohortRows = dashboardMapper.selectCohortSizes(actualQuery, cohortStartTime, endTime);
        List<Map<String, Object>> retentionRows = dashboardMapper.selectRetention(actualQuery,
            cohortStartTime, endTime, endTime);
        Map<String, Object> loginData = dashboardMapper.selectLoginSummary(actualQuery, funnelStartTime, endTime);

        Map<LocalDate, Long> cohortSizes = buildCohortSizes(cohortRows);
        Map<LocalDate, Map<Integer, Long>> retention = buildRetentionMap(retentionRows);

        UserGrowthDashboardVO result = new UserGrowthDashboardVO();
        result.setStartDate(funnelStartDate.format(DATE_FORMATTER));
        result.setEndDate(anchorDate.format(DATE_FORMATTER));
        result.setUpdateTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        result.setFunnel(buildFunnel(funnelData));
        result.setRetentionCurve(buildRetentionCurve(anchorDate, cohortSizes, retention));
        result.setRetentionMatrix(buildRetentionMatrix(anchorDate, cohortSizes, retention));
        fillLoginSummary(result, loginData,
            dashboardMapper.selectThirdLoginChannels(actualQuery, funnelStartTime, endTime));
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

    /**
     * 组装注册转化漏斗。
     */
    private List<UserGrowthDashboardVO.FunnelStage> buildFunnel(Map<String, Object> data)
    {
        long exposureUsers = number(data.get("exposureUsers"));
        List<UserGrowthDashboardVO.FunnelStage> result = new ArrayList<>();
        result.add(funnelStage("exposure", "注册页曝光", "U8", exposureUsers, exposureUsers));
        result.add(funnelStage("submit", "提交注册", "U8", number(data.get("submitUsers")), exposureUsers));
        result.add(funnelStage("success", "注册成功", "U8", number(data.get("successUsers")), exposureUsers));
        result.add(funnelStage("profile", "完善资料", "U11", number(data.get("completedUsers")), exposureUsers));
        return result;
    }

    /**
     * 创建注册漏斗阶段。
     */
    private UserGrowthDashboardVO.FunnelStage funnelStage(String code, String name, String metric,
        long value, long baseValue)
    {
        UserGrowthDashboardVO.FunnelStage stage = new UserGrowthDashboardVO.FunnelStage();
        stage.setCode(code);
        stage.setName(name);
        stage.setMetric(metric);
        stage.setValue(value);
        stage.setRate(percentage(value, baseValue));
        return stage;
    }

    /**
     * 将注册同期群查询结果转换为日期映射。
     */
    private Map<LocalDate, Long> buildCohortSizes(List<Map<String, Object>> rows)
    {
        Map<LocalDate, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows)
        {
            result.put(LocalDate.parse(String.valueOf(row.get("registerDate")), DATE_FORMATTER),
                number(row.get("registeredUsers")));
        }
        return result;
    }

    /**
     * 将留存查询结果转换为注册日期和天数偏移映射。
     */
    private Map<LocalDate, Map<Integer, Long>> buildRetentionMap(List<Map<String, Object>> rows)
    {
        Map<LocalDate, Map<Integer, Long>> result = new HashMap<>();
        for (Map<String, Object> row : rows)
        {
            LocalDate registerDate = LocalDate.parse(String.valueOf(row.get("registerDate")), DATE_FORMATTER);
            int dayOffset = (int) number(row.get("dayOffset"));
            result.computeIfAbsent(registerDate, key -> new HashMap<>())
                .put(dayOffset, number(row.get("retainedUsers")));
        }
        return result;
    }

    /**
     * 组装 D0 至 D30 留存曲线。
     */
    private List<UserGrowthDashboardVO.RetentionPoint> buildRetentionCurve(LocalDate anchorDate,
        Map<LocalDate, Long> cohortSizes, Map<LocalDate, Map<Integer, Long>> retention)
    {
        List<UserGrowthDashboardVO.RetentionPoint> result = new ArrayList<>();
        for (int day = 0; day <= MAX_RETENTION_DAY; day++)
        {
            long registeredUsers = 0;
            long retainedUsers = 0;
            LocalDate latestEligibleDate = anchorDate.minusDays(day);
            for (Map.Entry<LocalDate, Long> cohort : cohortSizes.entrySet())
            {
                if (!cohort.getKey().isAfter(latestEligibleDate))
                {
                    registeredUsers += cohort.getValue();
                    retainedUsers += day == 0 ? cohort.getValue()
                        : retention.getOrDefault(cohort.getKey(), Map.of()).getOrDefault(day, 0L);
                }
            }
            UserGrowthDashboardVO.RetentionPoint point = new UserGrowthDashboardVO.RetentionPoint();
            point.setDay(day);
            point.setRate(percentage(retainedUsers, registeredUsers));
            result.add(point);
        }
        return result;
    }

    /**
     * 组装最近七日留存矩阵。
     */
    private List<UserGrowthDashboardVO.CohortRow> buildRetentionMatrix(LocalDate anchorDate,
        Map<LocalDate, Long> cohortSizes, Map<LocalDate, Map<Integer, Long>> retention)
    {
        List<UserGrowthDashboardVO.CohortRow> result = new ArrayList<>();
        for (int dateOffset = MATRIX_DAYS - 1; dateOffset >= 0; dateOffset--)
        {
            LocalDate registerDate = anchorDate.minusDays(dateOffset);
            long registeredUsers = cohortSizes.getOrDefault(registerDate, 0L);
            UserGrowthDashboardVO.CohortRow row = new UserGrowthDashboardVO.CohortRow();
            row.setRegisterDate(registerDate.format(DATE_FORMATTER));
            row.setRegisteredUsers(registeredUsers);
            List<BigDecimal> rates = new ArrayList<>();
            for (int day = 0; day < MATRIX_DAYS; day++)
            {
                rates.add(registerDate.plusDays(day).isAfter(anchorDate) ? null
                    : percentage(day == 0 ? registeredUsers
                        : retention.getOrDefault(registerDate, Map.of()).getOrDefault(day, 0L), registeredUsers));
            }
            row.setRates(rates);
            result.add(row);
        }
        return result;
    }

    /**
     * 填充登录成功率和第三方登录占比。
     */
    private void fillLoginSummary(UserGrowthDashboardVO result, Map<String, Object> data,
        List<Map<String, Object>> channelRows)
    {
        long loginSuccesses = number(data.get("loginSuccesses"));
        long thirdLoginSuccesses = number(data.get("thirdLoginSuccesses"));
        result.setLoginSuccesses(loginSuccesses);
        result.setThirdLoginRate(percentage(thirdLoginSuccesses, loginSuccesses));

        List<UserGrowthDashboardVO.LoginChannel> channels = new ArrayList<>();
        for (Map<String, Object> row : channelRows)
        {
            UserGrowthDashboardVO.LoginChannel channel = new UserGrowthDashboardVO.LoginChannel();
            channel.setName(String.valueOf(row.get("channelName")));
            channel.setValue(number(row.get("loginCount")));
            channel.setRate(percentage(channel.getValue(), thirdLoginSuccesses));
            channels.add(channel);
        }
        result.setThirdLoginChannels(channels);
    }

    /**
     * 计算百分比，分母为零时返回零。
     */
    private BigDecimal percentage(long value, long baseValue)
    {
        if (baseValue <= 0)
        {
            return BigDecimal.ZERO.setScale(PERCENT_SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value).multiply(PERCENT_BASE)
            .divide(BigDecimal.valueOf(baseValue), PERCENT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 将数据库数值转换为长整型。
     */
    private long number(Object value)
    {
        if (value == null)
        {
            return 0L;
        }
        return new BigDecimal(String.valueOf(value)).longValue();
    }
}
