package com.woha.business.bi.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 平台大盘聚合数据。
 *
 * @author luowenshen
 */
public class PlatformDashboardVO
{
    /** 数据统计开始时间 */
    private String startTime;

    /** 数据统计结束时间 */
    private String endTime;

    /** 数据更新时间 */
    private String updateTime;

    /** KPI 指标集合 */
    private List<KpiItem> kpis = new ArrayList<>();

    /** 近 30 日充值与 DAU 趋势 */
    private List<TrendPoint> trends = new ArrayList<>();

    /** 最近一小时在线人数趋势 */
    private List<OnlinePoint> onlineTrends = new ArrayList<>();

    /** 地区充值金额 Top 榜 */
    private List<RegionRank> regionRanks = new ArrayList<>();

    public String getStartTime()
    {
        return startTime;
    }

    public void setStartTime(String startTime)
    {
        this.startTime = startTime;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }

    public String getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(String updateTime)
    {
        this.updateTime = updateTime;
    }

    public List<KpiItem> getKpis()
    {
        return kpis;
    }

    public void setKpis(List<KpiItem> kpis)
    {
        this.kpis = kpis;
    }

    public List<TrendPoint> getTrends()
    {
        return trends;
    }

    public void setTrends(List<TrendPoint> trends)
    {
        this.trends = trends;
    }

    public List<OnlinePoint> getOnlineTrends()
    {
        return onlineTrends;
    }

    public void setOnlineTrends(List<OnlinePoint> onlineTrends)
    {
        this.onlineTrends = onlineTrends;
    }

    public List<RegionRank> getRegionRanks()
    {
        return regionRanks;
    }

    public void setRegionRanks(List<RegionRank> regionRanks)
    {
        this.regionRanks = regionRanks;
    }

    /**
     * KPI 指标项。
     *
     * @author luowenshen
     */
    public static class KpiItem
    {
        /** 指标业务编码 */
        private String code;

        /** 指标展示名称 */
        private String name;

        /** 指标口径标识 */
        private String metric;

        /** 原始数值 */
        private BigDecimal value;

        /** 数值展示单位 */
        private String unit;

        /** 数据时效标签 */
        private String freshness;

        /** 是否触发异常提示 */
        private boolean abnormal;

        /** 相对基线变化比例 */
        private BigDecimal changeRate;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getMetric() { return metric; }
        public void setMetric(String metric) { this.metric = metric; }
        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getFreshness() { return freshness; }
        public void setFreshness(String freshness) { this.freshness = freshness; }
        public boolean isAbnormal() { return abnormal; }
        public void setAbnormal(boolean abnormal) { this.abnormal = abnormal; }
        public BigDecimal getChangeRate() { return changeRate; }
        public void setChangeRate(BigDecimal changeRate) { this.changeRate = changeRate; }
    }

    /**
     * 日趋势数据点。
     *
     * @author luowenshen
     */
    public static class TrendPoint
    {
        /** 统计日期 */
        private String date;

        /** 充值金额，单位为美元 */
        private BigDecimal gmv;

        /** 日活跃用户数 */
        private Long dau;

        /** 平台毛利，单位为美元 */
        private BigDecimal grossProfit;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public BigDecimal getGmv() { return gmv; }
        public void setGmv(BigDecimal gmv) { this.gmv = gmv; }
        public Long getDau() { return dau; }
        public void setDau(Long dau) { this.dau = dau; }
        public BigDecimal getGrossProfit() { return grossProfit; }
        public void setGrossProfit(BigDecimal grossProfit) { this.grossProfit = grossProfit; }
    }

    /**
     * 在线趋势数据点。
     *
     * @author luowenshen
     */
    public static class OnlinePoint
    {
        /** 五分钟时间点 */
        private String time;

        /** 时间点在线人数 */
        private Long onlineUsers;

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public Long getOnlineUsers() { return onlineUsers; }
        public void setOnlineUsers(Long onlineUsers) { this.onlineUsers = onlineUsers; }
    }

    /**
     * 地区充值排名项。
     *
     * @author luowenshen
     */
    public static class RegionRank
    {
        /** 地区国家编码 */
        private String country;

        /** 充值金额，单位为美元 */
        private BigDecimal amount;

        /** 充值用户数 */
        private Long payUsers;

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Long getPayUsers() { return payUsers; }
        public void setPayUsers(Long payUsers) { this.payUsers = payUsers; }
    }
}
