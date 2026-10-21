package com.woha.business.bi.domain;

/**
 * 邀请增长看板查询参数。
 *
 * @author luowenshen
 */
public class InviteGrowthQuery
{
    /** 业务区域编号。 */
    private Integer regionId;

    /** 受邀用户地区，为空时查询全部地区 */
    private String country;

    /** 受邀用户下载渠道，为空时查询全部渠道 */
    private String channel;

    /** 受邀用户操作系统，为空时查询全部系统 */
    private String os;

    /** 查询锚点日期，格式为 yyyy-MM-dd */
    private String date;

    public Integer getRegionId() { return regionId; }
    public void setRegionId(Integer regionId) { this.regionId = regionId; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
