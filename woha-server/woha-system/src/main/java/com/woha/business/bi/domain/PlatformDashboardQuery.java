package com.woha.business.bi.domain;

/**
 * 平台大盘查询参数。
 *
 * @author luowenshen
 */
public class PlatformDashboardQuery
{
    /** 业务区域编号。 */
    private Integer regionId;

    /** 地区国家编码，为空时查询全部地区 */
    private String country;

    /** 用户来源渠道，为空时查询全部渠道 */
    private String channel;

    /** 时间粒度，支持 realtime、day、week、month */
    private String period;

    /** 查询锚点日期，格式为 yyyy-MM-dd */
    private String date;

    public Integer getRegionId() { return regionId; }
    public void setRegionId(Integer regionId) { this.regionId = regionId; }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public String getChannel()
    {
        return channel;
    }

    public void setChannel(String channel)
    {
        this.channel = channel;
    }

    public String getPeriod()
    {
        return period;
    }

    public void setPeriod(String period)
    {
        this.period = period;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }
}
