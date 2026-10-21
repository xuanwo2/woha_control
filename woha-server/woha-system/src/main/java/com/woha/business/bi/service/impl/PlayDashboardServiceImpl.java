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
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.woha.business.bi.domain.PlayDashboardQuery;
import com.woha.business.bi.domain.PlayDashboardVO;
import com.woha.business.bi.domain.PlayGameOverviewVO;
import com.woha.business.bi.mapper.PlayDashboardMapper;
import com.woha.business.bi.service.IPlayDashboardService;
import com.woha.business.bi.service.DashboardRegionSupport;
import com.woha.business.common.annotation.DataSource;
import com.woha.business.common.enums.DataSourceType;
import com.woha.business.common.exception.ServiceException;

/**
 * 玩法看板服务实现。
 *
 * @author luowenshen
 */
@Service
@DataSource(DataSourceType.SLAVE)
public class PlayDashboardServiceImpl implements IPlayDashboardService
{
    /** 默认查询最近七天 */
    private static final int DEFAULT_DAYS = 7;
    /** 单次查询允许的最大天数 */
    private static final int MAX_QUERY_DAYS = 366;
    /** RTP 百分比计算基数 */
    private static final BigDecimal PERCENT_BASE = BigDecimal.valueOf(100);
    /** RTP 预警阈值，当前按百分之九十执行 */
    private static final BigDecimal RTP_ALERT_THRESHOLD = BigDecimal.valueOf(90);
    /** 比例指标保留小数位数 */
    private static final int RATE_SCALE = 2;
    /** 日期格式化规则 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** 日期时间格式化规则 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 玩法看板数据访问层 */
    private final PlayDashboardMapper dashboardMapper;

    public PlayDashboardServiceImpl(PlayDashboardMapper dashboardMapper)
    {
        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public PlayDashboardVO selectDashboard(PlayDashboardQuery query)
    {
        QueryContext context = resolveQuery(query);
        PlayDashboardVO result = new PlayDashboardVO();
        result.setStartDate(context.query.getStartDate());
        result.setEndDate(context.query.getEndDate());
        result.setUpdateTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        result.setGames(buildGameOverview(dashboardMapper.selectGameOverview(context.query,
            context.startTime, context.endTime)));
        result.setLuckSegments(buildLuckSegments(dashboardMapper.selectLuckSegments(context.query,
            context.startTime, context.endTime)));
        result.setLuckFunnel(buildFunnel(dashboardMapper.selectLuckFunnel(context.query,
            context.startTime, context.endTime)));
        return result;
    }

    @Override
    public List<PlayGameOverviewVO> selectGameOverview(PlayDashboardQuery query)
    {
        QueryContext context = resolveQuery(query);
        return buildGameOverview(dashboardMapper.selectGameOverview(context.query,
            context.startTime, context.endTime));
    }

    @Override
    public List<String> selectCountries(Integer regionId)
    {
        return dashboardMapper.selectCountries(DashboardRegionSupport.resolve(regionId));
    }

    /** 组装玩法总览指标。 */
    private List<PlayGameOverviewVO> buildGameOverview(List<Map<String, Object>> rows)
    {
        List<PlayGameOverviewVO> result = new ArrayList<>();
        for (Map<String, Object> row : rows)
        {
            String code = String.valueOf(row.get("gameCode"));
            BigDecimal betGold = decimal(row.get("betGold"));
            BigDecimal rewardGold = decimal(row.get("rewardGold"));
            boolean reconciliationOnly = "third".equals(code);
            BigDecimal rtp = reconciliationOnly ? null : ratio(rewardGold, betGold);

            PlayGameOverviewVO item = new PlayGameOverviewVO();
            item.setGameCode(code);
            item.setGameName(gameName(code));
            item.setBetGold(betGold);
            item.setRewardGold(reconciliationOnly ? null : rewardGold);
            item.setRtp(rtp);
            item.setMarginGold(betGold.subtract(rewardGold));
            item.setParticipants(number(row.get("participants")));
            item.setReconciliationOnly(reconciliationOnly);
            fillStatus(item);
            result.add(item);
        }
        return result;
    }

    /** 填充玩法运行状态。 */
    private void fillStatus(PlayGameOverviewVO item)
    {
        if (item.isReconciliationOnly())
        {
            item.setStatusCode("reconciliation");
            item.setStatusName("对账口径");
        }
        else if (item.getRtp() != null && item.getRtp().compareTo(RTP_ALERT_THRESHOLD) > 0)
        {
            item.setStatusCode("warning");
            item.setStatusName("RTP 超阈值预警");
        }
        else
        {
            item.setStatusCode("normal");
            item.setStatusName("正常");
        }
    }

    /** 组装砸蛋拆分口径。 */
    private List<PlayDashboardVO.LuckSegment> buildLuckSegments(List<Map<String, Object>> rows)
    {
        List<PlayDashboardVO.LuckSegment> result = new ArrayList<>();
        for (Map<String, Object> row : rows)
        {
            String code = String.valueOf(row.get("segmentCode"));
            BigDecimal input = decimal(row.get("inputGold"));
            BigDecimal output = decimal(row.get("outputGold"));
            PlayDashboardVO.LuckSegment item = new PlayDashboardVO.LuckSegment();
            item.setCode(code);
            item.setName(segmentName(code));
            item.setCategory(code.startsWith("fixed") || "personalPool".equals(code) ? "补偿" : "付费");
            item.setPlays(number(row.get("plays")));
            item.setRtp(ratio(output, input));
            item.setCostGold(output);
            item.setTag(segmentTag(code));
            result.add(item);
        }
        return result;
    }

    /** 组装砸蛋转化漏斗。 */
    private List<PlayDashboardVO.FunnelStage> buildFunnel(Map<String, Object> row)
    {
        List<PlayDashboardVO.FunnelStage> result = new ArrayList<>();
        result.add(funnelStage("exposure", "曝光", null, false));
        result.add(funnelStage("enter", "进入", null, false));
        result.add(funnelStage("bet", "投注", number(row.get("betUsers")), true));
        result.add(funnelStage("repeat", "复投（同日≥2次）", number(row.get("repeatUsers")), true));
        return result;
    }

    /** 创建砸蛋漏斗阶段。 */
    private PlayDashboardVO.FunnelStage funnelStage(String code, String name, Long value, boolean available)
    {
        PlayDashboardVO.FunnelStage item = new PlayDashboardVO.FunnelStage();
        item.setCode(code);
        item.setName(name);
        item.setValue(value);
        item.setAvailable(available);
        return item;
    }

    /** 返回玩法展示名称。 */
    private String gameName(String code)
    {
        return switch (code)
        {
            case "luck" -> "砸蛋 luck";
            case "wheel" -> "摩天轮 wheel";
            case "zool" -> "水果机 zool";
            case "tarot" -> "塔罗牌 tarot";
            case "third" -> "三方 yomi/cuizi/joy（P16）";
            default -> code;
        };
    }

    /** 返回砸蛋口径展示名称。 */
    private String segmentName(String code)
    {
        return switch (code)
        {
            case "probability" -> "付费 · 概率口径";
            case "publicPool" -> "付费 · 公共奖池";
            case "personalPool" -> "补偿 · 个人奖池";
            case "fixedManual" -> "补偿 · 手动定投";
            case "fixedAuto" -> "补偿 · 自动定投";
            default -> code;
        };
    }

    /** 返回砸蛋口径标签。 */
    private String segmentTag(String code)
    {
        return switch (code)
        {
            case "probability" -> "默认";
            case "publicPool" -> "公共池";
            case "personalPool" -> "个人池";
            case "fixedManual" -> "人工";
            case "fixedAuto" -> "自动";
            default -> "其他";
        };
    }

    /** 解析并校验查询上下文。 */
    private QueryContext resolveQuery(PlayDashboardQuery query)
    {
        PlayDashboardQuery actualQuery = query == null ? new PlayDashboardQuery() : query;
        actualQuery.setRegionId(DashboardRegionSupport.resolve(actualQuery.getRegionId()));
        LocalDate endDate = parseDate(actualQuery.getEndDate(), LocalDate.now(), "结束日期");
        LocalDate startDate = parseDate(actualQuery.getStartDate(), endDate.minusDays(DEFAULT_DAYS - 1L), "开始日期");
        validateRange(startDate, endDate);
        actualQuery.setStartDate(startDate.format(DATE_FORMATTER));
        actualQuery.setEndDate(endDate.format(DATE_FORMATTER));
        return new QueryContext(actualQuery, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
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

    /** 计算 RTP 百分比。 */
    private BigDecimal ratio(BigDecimal output, BigDecimal input)
    {
        return input.signum() == 0 ? BigDecimal.ZERO.setScale(RATE_SCALE)
            : output.multiply(PERCENT_BASE).divide(input, RATE_SCALE, RoundingMode.HALF_UP);
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

    /**
     * 已校验的玩法查询上下文。
     *
     * @author luowenshen
     */
    private static class QueryContext
    {
        /** 规范化后的查询参数 */
        private final PlayDashboardQuery query;
        /** 统计开始时间 */
        private final LocalDateTime startTime;
        /** 统计结束时间 */
        private final LocalDateTime endTime;

        QueryContext(PlayDashboardQuery query, LocalDateTime startTime, LocalDateTime endTime)
        {
            this.query = query;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
