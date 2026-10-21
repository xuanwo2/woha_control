package com.woha.business.bi.domain;

import java.util.List;
import com.woha.business.common.core.domain.BaseEntity;

/**
 * 埋点事件字典。
 *
 * @author luowenshen
 */
public class TrackingEvent extends BaseEntity
{
    /** 序列化版本号。 */
    private static final long serialVersionUID = 1L;

    /** 事件编号。 */
    private Long eventId;

    /** 业务模块。 */
    private String moduleName;

    /** 事件优先级。 */
    private String priority;

    /** 事件中文标签。 */
    private String eventLabel;

    /** 事件名称。 */
    private String eventName;

    /** 上报端。 */
    private String reportTerminal;

    /** 触发时机。 */
    private String triggerTiming;

    /** 引用指标编号，多个编号使用空格分隔。 */
    private String metricCodes;

    /** 字典版本。 */
    private String versionNo;

    /** 状态，0 表示启用，1 表示停用，2 表示已废弃。 */
    private String status;

    /** 变更原因，仅在提交修改审批时使用。 */
    private String changeReason;

    /** 事件私有参数列表。 */
    private List<TrackingEventParam> eventParams;

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getEventLabel() { return eventLabel; }
    public void setEventLabel(String eventLabel) { this.eventLabel = eventLabel; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getReportTerminal() { return reportTerminal; }
    public void setReportTerminal(String reportTerminal) { this.reportTerminal = reportTerminal; }
    public String getTriggerTiming() { return triggerTiming; }
    public void setTriggerTiming(String triggerTiming) { this.triggerTiming = triggerTiming; }
    public String getMetricCodes() { return metricCodes; }
    public void setMetricCodes(String metricCodes) { this.metricCodes = metricCodes; }
    public String getVersionNo() { return versionNo; }
    public void setVersionNo(String versionNo) { this.versionNo = versionNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public List<TrackingEventParam> getEventParams() { return eventParams; }
    public void setEventParams(List<TrackingEventParam> eventParams) { this.eventParams = eventParams; }
}
