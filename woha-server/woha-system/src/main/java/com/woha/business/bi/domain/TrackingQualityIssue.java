package com.woha.business.bi.domain;

import com.woha.business.common.core.domain.BaseEntity;

/**
 * 埋点质量处理工单。
 *
 * @author luowenshen
 */
public class TrackingQualityIssue extends BaseEntity {

    /** 序列化版本号。 */
    private static final long serialVersionUID = 1L;

    /** 工单编号。 */
    private Long issueId;

    /** 业务区域编号。 */
    private Integer regionId;

    /** 事件名称。 */
    private String eventName;

    /** App 版本号。 */
    private String appVersion;

    /** 问题类型。 */
    private String issueType;

    /** 严重级别，取值为 HIGH、MEDIUM 或 LOW。 */
    private String severity;

    /** 工单标题。 */
    private String title;

    /** 问题描述。 */
    private String description;

    /** 处理状态，0 待处理，1 处理中，2 已解决，3 已忽略。 */
    private String status;

    /** 处理负责人。 */
    private String assignee;

    /** 处理说明。 */
    private String resolution;

    /** 自动检测来源唯一键。 */
    private String sourceKey;

    /** 重复检测次数。 */
    private Long occurrenceCount;

    /** 最近检测时间。 */
    private String lastDetectTime;

    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
    public Integer getRegionId() { return regionId; }
    public void setRegionId(Integer regionId) { this.regionId = regionId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }
    public Long getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(Long occurrenceCount) { this.occurrenceCount = occurrenceCount; }
    public String getLastDetectTime() { return lastDetectTime; }
    public void setLastDetectTime(String lastDetectTime) { this.lastDetectTime = lastDetectTime; }
}
