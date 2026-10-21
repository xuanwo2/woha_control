package com.woha.business.bi.domain;

import java.math.BigDecimal;

/**
 * 单个埋点事件质量指标。
 *
 * @author luowenshen
 */
public class TrackingQualityEventVO {

    /** 事件名称。 */
    private String eventName;

    /** 近二十四小时上报量。 */
    private Long reportCount;

    /** 上报量环比。 */
    private BigDecimal changeRate;

    /** 漏报检测结论。 */
    private String leakStatus;

    /** 参数校验错误率。 */
    private BigDecimal errorRate;

    /** 采集链路 P95 延迟，单位为毫秒。 */
    private Long p95Latency;

    /** 质量状态，取值为 HEALTHY、RISK 或 WAITING。 */
    private String healthStatus;

    /** 关联待办工单编号。 */
    private Long issueId;

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public Long getReportCount() { return reportCount; }
    public void setReportCount(Long reportCount) { this.reportCount = reportCount; }
    public BigDecimal getChangeRate() { return changeRate; }
    public void setChangeRate(BigDecimal changeRate) { this.changeRate = changeRate; }
    public String getLeakStatus() { return leakStatus; }
    public void setLeakStatus(String leakStatus) { this.leakStatus = leakStatus; }
    public BigDecimal getErrorRate() { return errorRate; }
    public void setErrorRate(BigDecimal errorRate) { this.errorRate = errorRate; }
    public Long getP95Latency() { return p95Latency; }
    public void setP95Latency(Long p95Latency) { this.p95Latency = p95Latency; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
}
