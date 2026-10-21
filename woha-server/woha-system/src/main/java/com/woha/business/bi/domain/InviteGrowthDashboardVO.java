package com.woha.business.bi.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 邀请增长看板聚合数据。
 *
 * @author luowenshen
 */
public class InviteGrowthDashboardVO
{
    /** 数据统计开始日期 */
    private String startDate;

    /** 数据统计结束日期 */
    private String endDate;

    /** 数据更新时间 */
    private String updateTime;

    /** 今日邀请绑定用户数 */
    private Long todayBindings;

    /** 最近七日产生有效绑定的邀请人数 */
    private Long activeInviters;

    /** 最近七日 K 因子 */
    private BigDecimal kFactor;

    /** 分享点击是否已有可用数据源 */
    private boolean shareClickAvailable;

    /** 分享点击次数，数据源未接入时为空 */
    private Long shareClicks;

    /** 邀请新用户次日留存率 */
    private BigDecimal invitedD1Retention;

    /** 最近三十日邀请绑定趋势 */
    private List<TrendPoint> bindingTrends = new ArrayList<>();

    /** 邀请漏斗阶段 */
    private List<FunnelStage> funnel = new ArrayList<>();

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public Long getTodayBindings() { return todayBindings; }
    public void setTodayBindings(Long todayBindings) { this.todayBindings = todayBindings; }
    public Long getActiveInviters() { return activeInviters; }
    public void setActiveInviters(Long activeInviters) { this.activeInviters = activeInviters; }
    public BigDecimal getKFactor() { return kFactor; }
    public void setKFactor(BigDecimal kFactor) { this.kFactor = kFactor; }
    public boolean isShareClickAvailable() { return shareClickAvailable; }
    public void setShareClickAvailable(boolean shareClickAvailable) { this.shareClickAvailable = shareClickAvailable; }
    public Long getShareClicks() { return shareClicks; }
    public void setShareClicks(Long shareClicks) { this.shareClicks = shareClicks; }
    public BigDecimal getInvitedD1Retention() { return invitedD1Retention; }
    public void setInvitedD1Retention(BigDecimal invitedD1Retention) { this.invitedD1Retention = invitedD1Retention; }
    public List<TrendPoint> getBindingTrends() { return bindingTrends; }
    public void setBindingTrends(List<TrendPoint> bindingTrends) { this.bindingTrends = bindingTrends; }
    public List<FunnelStage> getFunnel() { return funnel; }
    public void setFunnel(List<FunnelStage> funnel) { this.funnel = funnel; }

    /**
     * 邀请绑定趋势数据点。
     *
     * @author luowenshen
     */
    public static class TrendPoint
    {
        /** 统计日期 */
        private String date;

        /** 当日绑定用户数 */
        private Long bindings;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Long getBindings() { return bindings; }
        public void setBindings(Long bindings) { this.bindings = bindings; }
    }

    /**
     * 邀请漏斗阶段。
     *
     * @author luowenshen
     */
    public static class FunnelStage
    {
        /** 阶段名称 */
        private String name;

        /** 阶段指标口径 */
        private String metric;

        /** 阶段数值，未接入数据源时为空 */
        private Long value;

        /** 阶段是否已接入数据源 */
        private boolean available;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getMetric() { return metric; }
        public void setMetric(String metric) { this.metric = metric; }
        public Long getValue() { return value; }
        public void setValue(Long value) { this.value = value; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }
}
