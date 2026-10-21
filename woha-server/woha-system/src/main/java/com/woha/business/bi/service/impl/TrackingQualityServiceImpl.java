package com.woha.business.bi.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.woha.business.bi.domain.TrackingAppRelease;
import com.woha.business.bi.domain.TrackingEvent;
import com.woha.business.bi.domain.TrackingQualityDashboardVO;
import com.woha.business.bi.domain.TrackingQualityEventVO;
import com.woha.business.bi.domain.TrackingQualityIssue;
import com.woha.business.bi.domain.TrackingQualityIssueUpdate;
import com.woha.business.bi.domain.TrackingQualityReleaseVO;
import com.woha.business.bi.domain.TrackingQualitySourceEventStat;
import com.woha.business.bi.mapper.TrackingQualityMapper;
import com.woha.business.bi.service.ITrackingQualityService;
import com.woha.business.bi.service.ITrackingQualitySourceService;
import com.woha.business.bi.service.DashboardRegionSupport;
import com.woha.business.common.exception.ServiceException;

/**
 * 埋点质量监控服务实现。
 *
 * @author luowenshen
 */
@Service
public class TrackingQualityServiceImpl implements ITrackingQualityService
{
    /** 漏报检测所需的前周期最小上报基线。 */
    private static final long LEAK_BASELINE_COUNT = 10L;

    /** 漏报检测下降比例阈值。 */
    private static final BigDecimal LEAK_DROP_THRESHOLD = new BigDecimal("0.50");

    /** 参数校验错误率告警阈值。 */
    private static final BigDecimal ERROR_RATE_THRESHOLD = new BigDecimal("0.05");

    /** 采集链路 P95 延迟告警阈值，单位为毫秒。 */
    private static final long LATENCY_THRESHOLD_MS = 3000L;

    /** 百分比换算基数。 */
    private static final BigDecimal PERCENT_BASE = new BigDecimal("100");

    /** 工单待处理状态。 */
    private static final String ISSUE_PENDING = "0";

    /** 工单处理中状态。 */
    private static final String ISSUE_PROCESSING = "1";

    /** 工单已解决状态。 */
    private static final String ISSUE_RESOLVED = "2";

    /** 工单已忽略状态。 */
    private static final String ISSUE_IGNORED = "3";

    /** 漏报问题类型。 */
    private static final String ISSUE_TYPE_LEAK = "LEAK";

    /** 参数错误问题类型。 */
    private static final String ISSUE_TYPE_PARAM_ERROR = "PARAM_ERROR";

    /** 链路延迟问题类型。 */
    private static final String ISSUE_TYPE_LATENCY = "LATENCY";

    /** 版本缺失问题类型。 */
    private static final String ISSUE_TYPE_RELEASE_MISSING = "RELEASE_MISSING";

    /** 健康质量状态。 */
    private static final String HEALTH_HEALTHY = "HEALTHY";

    /** 风险质量状态。 */
    private static final String HEALTH_RISK = "RISK";

    /** 已生成待办质量状态。 */
    private static final String HEALTH_WAITING = "WAITING";

    /** 正常漏报检测状态。 */
    private static final String LEAK_NORMAL = "NORMAL";

    /** 上报量明显下降状态。 */
    private static final String LEAK_WARNING = "WARNING";

    /** 完全未上报状态。 */
    private static final String LEAK_MISSING = "MISSING";

    /** 高严重级别。 */
    private static final String SEVERITY_HIGH = "HIGH";

    /** 中严重级别。 */
    private static final String SEVERITY_MEDIUM = "MEDIUM";

    /** 版本发布时间格式。 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 允许的工单状态集合。 */
    private static final Set<String> ISSUE_STATUSES = Set.of(
        ISSUE_PENDING, ISSUE_PROCESSING, ISSUE_RESOLVED, ISSUE_IGNORED);

    /** 埋点质量监控主库数据访问层。 */
    private final TrackingQualityMapper qualityMapper;

    /** 业务库埋点质量数据服务。 */
    private final ITrackingQualitySourceService sourceService;

    public TrackingQualityServiceImpl(TrackingQualityMapper qualityMapper,
        ITrackingQualitySourceService sourceService)
    {
        this.qualityMapper = qualityMapper;
        this.sourceService = sourceService;
    }

    @Override
    public TrackingQualityDashboardVO selectDashboard(Integer regionId)
    {
        Integer actualRegionId = DashboardRegionSupport.resolve(regionId);
        List<TrackingEvent> dictionaryEvents = qualityMapper.selectEnabledEvents();
        List<TrackingQualitySourceEventStat> sourceStats = sourceService.selectEventStats(actualRegionId);
        Map<String, TrackingQualitySourceEventStat> statMap = new HashMap<>();
        for (TrackingQualitySourceEventStat stat : sourceStats)
        {
            statMap.put(stat.getEventName(), stat);
        }

        Map<String, TrackingQualityIssue> activeIssueMap = buildActiveIssueMap(
            qualityMapper.selectActiveIssues(actualRegionId));
        List<TrackingQualityEventVO> eventRows = new ArrayList<>();
        long coveredEventCount = 0L;
        for (TrackingEvent event : dictionaryEvents)
        {
            TrackingQualitySourceEventStat stat = statMap.getOrDefault(event.getEventName(),
                emptyStat(event.getEventName()));
            if (value(stat.getCurrentCount()) > 0)
            {
                coveredEventCount++;
            }
            eventRows.add(buildEventRow(stat, activeIssueMap.get(event.getEventName())));
        }

        Map<String, Object> totals = sourceService.selectRecentTotals(actualRegionId);
        long successCount = longValue(totals, "successCount");
        long rejectCount = longValue(totals, "rejectCount");
        TrackingQualityDashboardVO result = new TrackingQualityDashboardVO();
        result.setDictionaryCoverage(percent(coveredEventCount, dictionaryEvents.size()));
        result.setTodayReports(value(sourceService.selectTodayReports(actualRegionId)));
        result.setValidationFailureRate(percent(rejectCount, successCount + rejectCount));
        result.setPendingIssueCount(value(qualityMapper.countPendingIssues(actualRegionId)));
        result.setUpdateTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        result.setEvents(eventRows);
        result.setReleases(buildReleaseRows(actualRegionId));
        return result;
    }

    @Override
    public int refreshQualityIssues(Integer regionId, String operator)
    {
        Integer actualRegionId = DashboardRegionSupport.resolve(regionId);
        String actualOperator = isBlank(operator) ? "system" : operator;
        Map<String, TrackingEvent> eventMap = new HashMap<>();
        for (TrackingEvent event : qualityMapper.selectEnabledEvents())
        {
            eventMap.put(event.getEventName(), event);
        }

        int affectedRows = 0;
        for (TrackingQualitySourceEventStat stat : sourceService.selectEventStats(actualRegionId))
        {
            TrackingEvent event = eventMap.get(stat.getEventName());
            if (event == null)
            {
                continue;
            }
            String assignee = resolveAssignee(event, actualOperator);
            if (isLeak(stat))
            {
                affectedRows += qualityMapper.upsertIssue(createLeakIssue(
                    actualRegionId, stat, assignee, actualOperator));
            }
            if (errorRate(stat).compareTo(ERROR_RATE_THRESHOLD) >= 0)
            {
                affectedRows += qualityMapper.upsertIssue(
                    createParamErrorIssue(actualRegionId, stat, assignee, actualOperator));
            }
            if (value(stat.getP95Latency()) >= LATENCY_THRESHOLD_MS)
            {
                affectedRows += qualityMapper.upsertIssue(
                    createLatencyIssue(actualRegionId, stat, assignee, actualOperator));
            }
        }

        for (TrackingQualityReleaseVO release : buildReleaseRows(actualRegionId))
        {
            for (String eventName : release.getMissingEvents())
            {
                TrackingEvent event = eventMap.get(eventName);
                String assignee = event == null ? actualOperator : resolveAssignee(event, actualOperator);
                affectedRows += qualityMapper.upsertIssue(createReleaseIssue(actualRegionId,
                    release.getVersionNo(), eventName, assignee, actualOperator));
            }
        }
        return affectedRows;
    }

    @Override
    public List<TrackingQualityIssue> selectIssueList(TrackingQualityIssue query)
    {
        TrackingQualityIssue actualQuery = query == null ? new TrackingQualityIssue() : query;
        actualQuery.setRegionId(DashboardRegionSupport.resolve(actualQuery.getRegionId()));
        return qualityMapper.selectIssueList(actualQuery);
    }

    @Override
    @Transactional
    public int updateIssue(Long issueId, Integer regionId, TrackingQualityIssueUpdate update,
        String operator)
    {
        Integer actualRegionId = DashboardRegionSupport.resolve(regionId);
        if (issueId == null || qualityMapper.selectIssueById(issueId, actualRegionId) == null)
        {
            throw new ServiceException("质量工单不存在");
        }
        if (update == null || !ISSUE_STATUSES.contains(update.getStatus()))
        {
            throw new ServiceException("工单状态不正确");
        }
        if (ISSUE_RESOLVED.equals(update.getStatus()) && isBlank(update.getResolution()))
        {
            throw new ServiceException("解决工单时必须填写处理说明");
        }
        return qualityMapper.updateIssue(issueId, actualRegionId, update,
            isBlank(operator) ? "system" : operator);
    }

    /** 构建事件质量展示行。 */
    private TrackingQualityEventVO buildEventRow(TrackingQualitySourceEventStat stat,
        TrackingQualityIssue activeIssue)
    {
        TrackingQualityEventVO row = new TrackingQualityEventVO();
        row.setEventName(stat.getEventName());
        row.setReportCount(value(stat.getCurrentCount()));
        row.setChangeRate(changePercent(stat));
        row.setLeakStatus(resolveLeakStatus(stat));
        row.setErrorRate(errorRate(stat).multiply(PERCENT_BASE).setScale(2, RoundingMode.HALF_UP));
        row.setP95Latency(value(stat.getP95Latency()));
        row.setHealthStatus(activeIssue == null
            ? (hasRisk(stat) ? HEALTH_RISK : HEALTH_HEALTHY) : HEALTH_WAITING);
        row.setIssueId(activeIssue == null ? null : activeIssue.getIssueId());
        return row;
    }

    /** 构建最近发布版本核对结果。 */
    private List<TrackingQualityReleaseVO> buildReleaseRows(Integer regionId)
    {
        List<TrackingAppRelease> releases = sourceService.selectRecentReleases();
        if (releases == null || releases.isEmpty())
        {
            return Collections.emptyList();
        }
        List<TrackingQualityReleaseVO> result = new ArrayList<>();
        for (int index = 0; index < releases.size(); index++)
        {
            TrackingAppRelease release = releases.get(index);
            LocalDateTime endTime = parseDateTime(release.getPublishTime());
            LocalDateTime startTime = index + 1 < releases.size()
                ? parseDateTime(releases.get(index + 1).getPublishTime()) : null;
            List<String> expected = distinct(qualityMapper.selectExpectedReleaseEvents(startTime, endTime));
            List<String> actual = distinct(sourceService.selectVersionEvents(
                release.getVersionNo(), regionId));
            List<String> missing = new ArrayList<>(expected);
            missing.removeAll(new HashSet<>(actual));

            TrackingQualityReleaseVO row = new TrackingQualityReleaseVO();
            row.setVersionNo(release.getVersionNo());
            row.setPublishTime(release.getPublishTime());
            row.setExpectedEvents(expected);
            row.setActualEvents(actual);
            row.setMissingEvents(missing);
            if (expected.isEmpty())
            {
                row.setStatus("EMPTY");
                row.setConclusion("发布时间区间内没有已审批的埋点变更");
            }
            else if (missing.isEmpty())
            {
                row.setStatus("PASSED");
                row.setConclusion("本版本应发布事件均已检测到上报");
            }
            else
            {
                row.setStatus("WARNING");
                row.setConclusion("存在 " + missing.size() + " 个应发布事件尚未检测到上报");
            }
            result.add(row);
        }
        return result;
    }

    /** 按事件保留最高优先级的未完成工单。 */
    private Map<String, TrackingQualityIssue> buildActiveIssueMap(List<TrackingQualityIssue> issues)
    {
        Map<String, TrackingQualityIssue> result = new HashMap<>();
        for (TrackingQualityIssue issue : issues)
        {
            if (!isBlank(issue.getEventName()))
            {
                result.putIfAbsent(issue.getEventName(), issue);
            }
        }
        return result;
    }

    /** 创建漏报质量工单。 */
    private TrackingQualityIssue createLeakIssue(Integer regionId, TrackingQualitySourceEventStat stat,
        String assignee, String operator)
    {
        return issue(regionId, stat.getEventName(), null, ISSUE_TYPE_LEAK, SEVERITY_HIGH,
            "事件上报量异常下降",
            "事件 " + stat.getEventName() + " 近二十四小时上报 " + value(stat.getCurrentCount())
                + " 次，前二十四小时上报 " + value(stat.getPreviousCount()) + " 次。",
            ISSUE_TYPE_LEAK + ":" + stat.getEventName(), assignee, operator);
    }

    /** 创建参数校验错误质量工单。 */
    private TrackingQualityIssue createParamErrorIssue(Integer regionId,
        TrackingQualitySourceEventStat stat,
        String assignee, String operator)
    {
        BigDecimal rate = errorRate(stat).multiply(PERCENT_BASE).setScale(2, RoundingMode.HALF_UP);
        return issue(regionId, stat.getEventName(), null, ISSUE_TYPE_PARAM_ERROR, SEVERITY_HIGH,
            "事件参数校验失败率过高",
            "事件 " + stat.getEventName() + " 近二十四小时参数校验失败率为 " + rate + "% ，拒绝 "
                + value(stat.getRejectCount()) + " 次。",
            ISSUE_TYPE_PARAM_ERROR + ":" + stat.getEventName(), assignee, operator);
    }

    /** 创建链路延迟质量工单。 */
    private TrackingQualityIssue createLatencyIssue(Integer regionId,
        TrackingQualitySourceEventStat stat,
        String assignee, String operator)
    {
        return issue(regionId, stat.getEventName(), null, ISSUE_TYPE_LATENCY, SEVERITY_MEDIUM,
            "事件采集链路延迟过高",
            "事件 " + stat.getEventName() + " 近二十四小时 P95 延迟为 "
                + value(stat.getP95Latency()) + " 毫秒。",
            ISSUE_TYPE_LATENCY + ":" + stat.getEventName(), assignee, operator);
    }

    /** 创建版本事件缺失质量工单。 */
    private TrackingQualityIssue createReleaseIssue(Integer regionId, String versionNo, String eventName,
        String assignee, String operator)
    {
        return issue(regionId, eventName, versionNo, ISSUE_TYPE_RELEASE_MISSING, SEVERITY_HIGH,
            "App 版本应发布事件缺失",
            "App 版本 " + versionNo + " 应发布事件 " + eventName + "，当前尚未检测到上报。",
            ISSUE_TYPE_RELEASE_MISSING + ":" + versionNo + ":" + eventName,
            assignee, operator);
    }

    /** 创建通用质量工单对象。 */
    private TrackingQualityIssue issue(Integer regionId, String eventName, String appVersion, String issueType,
        String severity, String title, String description, String sourceKey,
        String assignee, String operator)
    {
        TrackingQualityIssue issue = new TrackingQualityIssue();
        issue.setRegionId(regionId);
        issue.setEventName(eventName);
        issue.setAppVersion(appVersion);
        issue.setIssueType(issueType);
        issue.setSeverity(severity);
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setSourceKey(regionId + ":" + sourceKey);
        issue.setAssignee(assignee);
        issue.setCreateBy(operator);
        return issue;
    }

    /** 判断事件是否满足漏报告警规则。 */
    private boolean isLeak(TrackingQualitySourceEventStat stat)
    {
        long previous = value(stat.getPreviousCount());
        if (previous < LEAK_BASELINE_COUNT)
        {
            return false;
        }
        BigDecimal currentRatio = BigDecimal.valueOf(value(stat.getCurrentCount()))
            .divide(BigDecimal.valueOf(previous), 4, RoundingMode.HALF_UP);
        return currentRatio.compareTo(BigDecimal.ONE.subtract(LEAK_DROP_THRESHOLD)) <= 0;
    }

    /** 判断事件是否存在任一质量风险。 */
    private boolean hasRisk(TrackingQualitySourceEventStat stat)
    {
        return isLeak(stat) || errorRate(stat).compareTo(ERROR_RATE_THRESHOLD) >= 0
            || value(stat.getP95Latency()) >= LATENCY_THRESHOLD_MS;
    }

    /** 计算参数校验错误率。 */
    private BigDecimal errorRate(TrackingQualitySourceEventStat stat)
    {
        long success = value(stat.getCurrentCount());
        long reject = value(stat.getRejectCount());
        long total = success + reject;
        return total == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(reject)
            .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
    }

    /** 计算事件上报量环比百分比。 */
    private BigDecimal changePercent(TrackingQualitySourceEventStat stat)
    {
        long previous = value(stat.getPreviousCount());
        if (previous == 0)
        {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value(stat.getCurrentCount()) - previous)
            .multiply(PERCENT_BASE)
            .divide(BigDecimal.valueOf(previous), 2, RoundingMode.HALF_UP);
    }

    /** 解析漏报检测展示状态。 */
    private String resolveLeakStatus(TrackingQualitySourceEventStat stat)
    {
        if (!isLeak(stat))
        {
            return LEAK_NORMAL;
        }
        return value(stat.getCurrentCount()) == 0 ? LEAK_MISSING : LEAK_WARNING;
    }

    /** 计算百分比。 */
    private BigDecimal percent(long numerator, long denominator)
    {
        return denominator == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(numerator)
            .multiply(PERCENT_BASE)
            .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    /** 解析版本发布时间。 */
    private LocalDateTime parseDateTime(String value)
    {
        try
        {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        }
        catch (DateTimeParseException exception)
        {
            throw new ServiceException("App 版本发布时间格式不正确");
        }
    }

    /** 去重并保留原始顺序。 */
    private List<String> distinct(List<String> values)
    {
        return values == null ? new ArrayList<>() : new ArrayList<>(new LinkedHashSet<>(values));
    }

    /** 创建空事件聚合数据。 */
    private TrackingQualitySourceEventStat emptyStat(String eventName)
    {
        TrackingQualitySourceEventStat stat = new TrackingQualitySourceEventStat();
        stat.setEventName(eventName);
        stat.setCurrentCount(0L);
        stat.setPreviousCount(0L);
        stat.setRejectCount(0L);
        stat.setP95Latency(0L);
        return stat;
    }

    /** 解析事件负责人。 */
    private String resolveAssignee(TrackingEvent event, String fallback)
    {
        if (!isBlank(event.getUpdateBy()))
        {
            return event.getUpdateBy();
        }
        return isBlank(event.getCreateBy()) ? fallback : event.getCreateBy();
    }

    /** 从聚合结果读取长整型数值。 */
    private long longValue(Map<String, Object> values, String key)
    {
        Object value = values == null ? null : values.get(key);
        if (value == null && values != null)
        {
            for (Map.Entry<String, Object> entry : values.entrySet())
            {
                if (entry.getKey().equalsIgnoreCase(key))
                {
                    value = entry.getValue();
                    break;
                }
            }
        }
        return value == null ? 0L : new BigDecimal(String.valueOf(value)).longValue();
    }

    /** 将可空长整型转换为零值安全结果。 */
    private long value(Long value)
    {
        return value == null ? 0L : value;
    }

    /** 判断文本是否为空。 */
    private boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }
}
