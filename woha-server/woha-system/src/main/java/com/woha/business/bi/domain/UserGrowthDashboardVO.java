package com.woha.business.bi.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户增长看板聚合数据。
 *
 * @author luowenshen
 */
public class UserGrowthDashboardVO
{
    /** 数据统计开始日期 */
    private String startDate;

    /** 数据统计结束日期 */
    private String endDate;

    /** 数据更新时间 */
    private String updateTime;

    /** 注册转化漏斗 */
    private List<FunnelStage> funnel = new ArrayList<>();

    /** D0 至 D30 留存曲线 */
    private List<RetentionPoint> retentionCurve = new ArrayList<>();

    /** 最近七日留存矩阵 */
    private List<CohortRow> retentionMatrix = new ArrayList<>();

    /** 登录成功次数 */
    private Long loginSuccesses;

    /** 第三方登录占比 */
    private BigDecimal thirdLoginRate;

    /** 第三方登录渠道分布 */
    private List<LoginChannel> thirdLoginChannels = new ArrayList<>();

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public List<FunnelStage> getFunnel() { return funnel; }
    public void setFunnel(List<FunnelStage> funnel) { this.funnel = funnel; }
    public List<RetentionPoint> getRetentionCurve() { return retentionCurve; }
    public void setRetentionCurve(List<RetentionPoint> retentionCurve) { this.retentionCurve = retentionCurve; }
    public List<CohortRow> getRetentionMatrix() { return retentionMatrix; }
    public void setRetentionMatrix(List<CohortRow> retentionMatrix) { this.retentionMatrix = retentionMatrix; }
    public Long getLoginSuccesses() { return loginSuccesses; }
    public void setLoginSuccesses(Long loginSuccesses) { this.loginSuccesses = loginSuccesses; }
    public BigDecimal getThirdLoginRate() { return thirdLoginRate; }
    public void setThirdLoginRate(BigDecimal thirdLoginRate) { this.thirdLoginRate = thirdLoginRate; }
    public List<LoginChannel> getThirdLoginChannels() { return thirdLoginChannels; }
    public void setThirdLoginChannels(List<LoginChannel> thirdLoginChannels) { this.thirdLoginChannels = thirdLoginChannels; }

    /**
     * 注册漏斗阶段。
     *
     * @author luowenshen
     */
    public static class FunnelStage
    {
        /** 阶段业务编码 */
        private String code;

        /** 阶段展示名称 */
        private String name;

        /** 指标口径标识 */
        private String metric;

        /** 阶段用户数 */
        private Long value;

        /** 相对首阶段转化率 */
        private BigDecimal rate;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getMetric() { return metric; }
        public void setMetric(String metric) { this.metric = metric; }
        public Long getValue() { return value; }
        public void setValue(Long value) { this.value = value; }
        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }
    }

    /**
     * 留存曲线数据点。
     *
     * @author luowenshen
     */
    public static class RetentionPoint
    {
        /** 注册后的天数偏移 */
        private Integer day;

        /** 留存率 */
        private BigDecimal rate;

        public Integer getDay() { return day; }
        public void setDay(Integer day) { this.day = day; }
        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }
    }

    /**
     * 注册日留存矩阵行。
     *
     * @author luowenshen
     */
    public static class CohortRow
    {
        /** 注册日期 */
        private String registerDate;

        /** 注册用户数 */
        private Long registeredUsers;

        /** D0 至 D6 留存率，尚未到达的日期使用空值 */
        private List<BigDecimal> rates = new ArrayList<>();

        public String getRegisterDate() { return registerDate; }
        public void setRegisterDate(String registerDate) { this.registerDate = registerDate; }
        public Long getRegisteredUsers() { return registeredUsers; }
        public void setRegisteredUsers(Long registeredUsers) { this.registeredUsers = registeredUsers; }
        public List<BigDecimal> getRates() { return rates; }
        public void setRates(List<BigDecimal> rates) { this.rates = rates; }
    }

    /**
     * 第三方登录渠道统计项。
     *
     * @author luowenshen
     */
    public static class LoginChannel
    {
        /** 登录渠道名称 */
        private String name;

        /** 登录成功次数 */
        private Long value;

        /** 渠道占第三方登录比例 */
        private BigDecimal rate;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getValue() { return value; }
        public void setValue(Long value) { this.value = value; }
        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }
    }
}
