package com.woha.business.bi.domain;

/**
 * 业务库埋点事件质量聚合数据。
 *
 * @author luowenshen
 */
public class TrackingQualitySourceEventStat
{
    /** 事件名称。 */
    private String eventName;

    /** 近二十四小时成功上报量。 */
    private Long currentCount;

    /** 前二十四小时成功上报量。 */
    private Long previousCount;

    /** 近二十四小时校验拒绝量。 */
    private Long rejectCount;

    /** 近二十四小时采集链路 P95 延迟，单位为毫秒。 */
    private Long p95Latency;

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public Long getCurrentCount() { return currentCount; }
    public void setCurrentCount(Long currentCount) { this.currentCount = currentCount; }
    public Long getPreviousCount() { return previousCount; }
    public void setPreviousCount(Long previousCount) { this.previousCount = previousCount; }
    public Long getRejectCount() { return rejectCount; }
    public void setRejectCount(Long rejectCount) { this.rejectCount = rejectCount; }
    public Long getP95Latency() { return p95Latency; }
    public void setP95Latency(Long p95Latency) { this.p95Latency = p95Latency; }
}
