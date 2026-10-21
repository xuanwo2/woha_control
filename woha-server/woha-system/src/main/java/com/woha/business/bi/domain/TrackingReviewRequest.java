package com.woha.business.bi.domain;

/**
 * 埋点变更审批请求。
 *
 * @author luowenshen
 */
public class TrackingReviewRequest
{
    /** 审批结果，1 表示通过，2 表示驳回。 */
    private String approvalStatus;

    /** 审批意见。 */
    private String approvalRemark;

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public String getApprovalRemark() { return approvalRemark; }
    public void setApprovalRemark(String approvalRemark) { this.approvalRemark = approvalRemark; }
}
