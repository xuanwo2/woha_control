package com.woha.business.bi.service;

import com.woha.business.bi.domain.UserGrowthDashboardVO;
import com.woha.business.bi.domain.UserGrowthDimensionVO;
import com.woha.business.bi.domain.UserGrowthQuery;

/**
 * 用户增长看板服务。
 *
 * @author luowenshen
 */
public interface IUserGrowthDashboardService
{
    /**
     * 查询用户增长看板聚合数据。
     *
     * @param query 查询条件
     * @return 用户增长看板聚合数据
     */
    UserGrowthDashboardVO selectDashboard(UserGrowthQuery query);

    /**
     * 查询用户增长看板筛选维度。
     *
     * @param regionId 区域编号
     * @return 用户增长看板筛选维度
     */
    UserGrowthDimensionVO selectDimensions(Integer regionId);
}
