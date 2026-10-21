package com.woha.business.bi.domain;

/**
 * 玩法看板查询参数。
 *
 * @author luowenshen
 */
public class PlayDashboardQuery
{
    /** 业务区域编号。 */
    private Integer regionId;

    /** 统计开始日期，格式为 yyyy-MM-dd */
    private String startDate;

    /** 统计结束日期，格式为 yyyy-MM-dd */
    private String endDate;

    /** 用户所属地区，为空时查询全部地区 */
    private String country;

    public Integer getRegionId() { return regionId; }
    public void setRegionId(Integer regionId) { this.regionId = regionId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
