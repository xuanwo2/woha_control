package com.woha.business.bi.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户增长看板筛选维度。
 *
 * @author luowenshen
 */
public class UserGrowthDimensionVO
{
    /** 可选地区集合 */
    private List<String> countries = new ArrayList<>();

    /** 可选渠道集合 */
    private List<String> channels = new ArrayList<>();

    /** 可选操作系统集合 */
    private List<String> operatingSystems = new ArrayList<>();

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

    public List<String> getOperatingSystems()
    {
        return operatingSystems;
    }

    public void setOperatingSystems(List<String> operatingSystems)
    {
        this.operatingSystems = operatingSystems;
    }
}
