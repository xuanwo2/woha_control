package com.woha.business.bi.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 平台大盘筛选维度。
 *
 * @author luowenshen
 */
public class DashboardDimensionVO
{
    /** 地区国家编码列表 */
    private List<String> countries = new ArrayList<>();

    /** 用户来源渠道列表 */
    private List<String> channels = new ArrayList<>();

    public List<String> getCountries()
    {
        return countries;
    }

    public void setCountries(List<String> countries)
    {
        this.countries = countries;
    }

    public List<String> getChannels()
    {
        return channels;
    }

    public void setChannels(List<String> channels)
    {
        this.channels = channels;
    }
}
