package com.woha.business.bi.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * 埋点质量监控看板数据。
 *
 * @author luowenshen
 */
public class TrackingQualityDashboardVO {

    /** 事件字典覆盖率。 */
    private BigDecimal dictionaryCoverage;

    /** 今日事件上报总量。 */
    private Long todayReports;

    /** 近二十四小时参数校验失败率。 */
    private BigDecimal validationFailureRate;

    /** 待处理质量工单数量。 */
    private Long pendingIssueCount;

    /** 数据更新时间。 */
    private String updateTime;

    /** 按事件质量指标。 */
    private List<TrackingQualityEventVO> events;

    /** 版本发布核对结果。 */
    private List<TrackingQualityReleaseVO> releases;

    public BigDecimal getDictionaryCoverage() { return dictionaryCoverage; }
    public void setDictionaryCoverage(BigDecimal dictionaryCoverage) { this.dictionaryCoverage = dictionaryCoverage; }
    public Long getTodayReports() { return todayReports; }
    public void setTodayReports(Long todayReports) { this.todayReports = todayReports; }
    public BigDecimal getValidationFailureRate() { return validationFailureRate; }
    public void setValidationFailureRate(BigDecimal validationFailureRate) { this.validationFailureRate = validationFailureRate; }
    public Long getPendingIssueCount() { return pendingIssueCount; }
    public void setPendingIssueCount(Long pendingIssueCount) { this.pendingIssueCount = pendingIssueCount; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public List<TrackingQualityEventVO> getEvents() { return events; }
    public void setEvents(List<TrackingQualityEventVO> events) { this.events = events; }
    public List<TrackingQualityReleaseVO> getReleases() { return releases; }
    public void setReleases(List<TrackingQualityReleaseVO> releases) { this.releases = releases; }
}
