package com.woha.business.bi.domain;

/**
 * 埋点质量工单处理参数。
 *
 * @author luowenshen
 */
public class TrackingQualityIssueUpdate {

    /** 处理状态。 */
    private String status;

    /** 处理负责人。 */
    private String assignee;

    /** 处理说明。 */
    private String resolution;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
}
