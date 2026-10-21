package com.woha.business.bi.domain;

import com.woha.business.common.core.domain.BaseEntity;

/**
 * 埋点字典变更审批记录。
 *
 * @author luowenshen
 */
public class TrackingChangeRecord extends BaseEntity
{
    /** 序列化版本号。 */
    private static final long serialVersionUID = 1L;

    /** 变更记录编号。 */
    private Long changeId;

    /** 事件编号。 */
    private Long eventId;

    /** 事件名称。 */
    private String eventName;

    /** 变更类型。 */
    private String changeType;

    /** 变更原因。 */
    private String changeReason;

    /** 变更后的完整快照。 */
    private String snapshotJson;

    /** 审批状态，0 表示待审批，1 表示已通过，2 表示已驳回。 */
    private String approvalStatus;

    /** 审批人。 */
    private String approveBy;

    /** 审批时间。 */
    private String approveTime;

    /** 审批意见。 */
    private String approvalRemark;

    public Long getChangeId() { return changeId; }
    public void setChangeId(Long changeId) { this.changeId = changeId; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public String getSnapshotJson() { return snapshotJson; }
    public void setSnapshotJson(String snapshotJson) { this.snapshotJson = snapshotJson; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public String getApproveBy() { return approveBy; }
    public void setApproveBy(String approveBy) { this.approveBy = approveBy; }
    public String getApproveTime() { return approveTime; }
    public void setApproveTime(String approveTime) { this.approveTime = approveTime; }
    public String getApprovalRemark() { return approvalRemark; }
    public void setApprovalRemark(String approvalRemark) { this.approvalRemark = approvalRemark; }
}
