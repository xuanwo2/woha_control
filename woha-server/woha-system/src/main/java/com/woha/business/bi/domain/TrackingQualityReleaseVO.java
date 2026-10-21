package com.woha.business.bi.domain;

import java.util.List;

/**
 * App 版本埋点发布核对结果。
 *
 * @author luowenshen
 */
public class TrackingQualityReleaseVO {

    /** App 版本号。 */
    private String versionNo;

    /** 发布时间。 */
    private String publishTime;

    /** 本版本应新增或变更的事件。 */
    private List<String> expectedEvents;

    /** 实际检测到的事件。 */
    private List<String> actualEvents;

    /** 缺失事件。 */
    private List<String> missingEvents;

    /** 核对状态，取值为 PASSED、WARNING 或 EMPTY。 */
    private String status;

    /** 核对结论。 */
    private String conclusion;

    public String getVersionNo() { return versionNo; }
    public void setVersionNo(String versionNo) { this.versionNo = versionNo; }
    public String getPublishTime() { return publishTime; }
    public void setPublishTime(String publishTime) { this.publishTime = publishTime; }
    public List<String> getExpectedEvents() { return expectedEvents; }
    public void setExpectedEvents(List<String> expectedEvents) { this.expectedEvents = expectedEvents; }
    public List<String> getActualEvents() { return actualEvents; }
    public void setActualEvents(List<String> actualEvents) { this.actualEvents = actualEvents; }
    public List<String> getMissingEvents() { return missingEvents; }
    public void setMissingEvents(List<String> missingEvents) { this.missingEvents = missingEvents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }
}
