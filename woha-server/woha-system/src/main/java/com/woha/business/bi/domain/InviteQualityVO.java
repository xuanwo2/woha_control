package com.woha.business.bi.domain;

import java.math.BigDecimal;
import com.woha.business.common.annotation.Excel;

/**
 * 邀请人质量分层数据。
 *
 * @author luowenshen
 */
public class InviteQualityVO
{
    /** 邀请人 UID，仅用于生成脱敏展示值 */
    private Long inviterUid;

    /** 脱敏后的邀请人标识 */
    @Excel(name = "邀请人")
    private String inviter;

    /** 绑定新用户数 */
    @Excel(name = "绑定新用户")
    private Long boundUsers;

    /** 受邀用户次日留存率 */
    @Excel(name = "次日留存", suffix = "%", scale = 2)
    private BigDecimal d1Retention;

    /** 受邀用户付费率 */
    @Excel(name = "付费率", suffix = "%", scale = 2)
    private BigDecimal payRate;

    /** 受邀用户带来的充值金额，单位为美元 */
    @Excel(name = "带来 GMV", scale = 2)
    private BigDecimal gmv;

    /** 邀请用户中同一设备最大账号数 */
    private Long maxDeviceCluster;

    /** 设备聚集展示结果 */
    @Excel(name = "设备聚集")
    private String deviceStatus;

    /** 质量等级业务编码 */
    private String qualityCode;

    /** 质量等级展示名称 */
    @Excel(name = "质量评级")
    private String qualityName;

    public Long getInviterUid() { return inviterUid; }
    public void setInviterUid(Long inviterUid) { this.inviterUid = inviterUid; }
    public String getInviter() { return inviter; }
    public void setInviter(String inviter) { this.inviter = inviter; }
    public Long getBoundUsers() { return boundUsers; }
    public void setBoundUsers(Long boundUsers) { this.boundUsers = boundUsers; }
    public BigDecimal getD1Retention() { return d1Retention; }
    public void setD1Retention(BigDecimal d1Retention) { this.d1Retention = d1Retention; }
    public BigDecimal getPayRate() { return payRate; }
    public void setPayRate(BigDecimal payRate) { this.payRate = payRate; }
    public BigDecimal getGmv() { return gmv; }
    public void setGmv(BigDecimal gmv) { this.gmv = gmv; }
    public Long getMaxDeviceCluster() { return maxDeviceCluster; }
    public void setMaxDeviceCluster(Long maxDeviceCluster) { this.maxDeviceCluster = maxDeviceCluster; }
    public String getDeviceStatus() { return deviceStatus; }
    public void setDeviceStatus(String deviceStatus) { this.deviceStatus = deviceStatus; }
    public String getQualityCode() { return qualityCode; }
    public void setQualityCode(String qualityCode) { this.qualityCode = qualityCode; }
    public String getQualityName() { return qualityName; }
    public void setQualityName(String qualityName) { this.qualityName = qualityName; }
}
