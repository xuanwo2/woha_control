package com.woha.business.bi.domain;

/**
 * 埋点事件字典查询参数。
 *
 * @author luowenshen
 */
public class TrackingEventQuery
{
    /** 业务模块。 */
    private String moduleName;

    /** 上报端。 */
    private String reportTerminal;

    /** 事件名称或中文标签关键词。 */
    private String keyword;

    /** 事件状态。 */
    private String status;

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public String getReportTerminal() { return reportTerminal; }
    public void setReportTerminal(String reportTerminal) { this.reportTerminal = reportTerminal; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
