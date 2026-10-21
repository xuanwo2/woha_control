package com.woha.business.bi.domain;

/**
 * App 发布版本快照。
 *
 * @author luowenshen
 */
public class TrackingAppRelease
{
    /** App 版本号。 */
    private String versionNo;

    /** 发布时间。 */
    private String publishTime;

    public String getVersionNo() { return versionNo; }
    public void setVersionNo(String versionNo) { this.versionNo = versionNo; }
    public String getPublishTime() { return publishTime; }
    public void setPublishTime(String publishTime) { this.publishTime = publishTime; }
}
