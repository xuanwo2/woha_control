package com.woha.business.bi.service;

import com.woha.business.bi.domain.DashboardDimensionVO;
import com.woha.business.bi.domain.PlatformDashboardQuery;
import com.woha.business.bi.domain.PlatformDashboardVO;

/**
 * 平台大盘服务。
 *
 * @author luowenshen
 */
public interface IPlatformDashboardService
{
    /**
     * 查询平台大盘聚合数据。
     *
     * @param query 查询条件
     * @return 平台大盘聚合数据
     */
    PlatformDashboardVO selectDashboard(PlatformDashboardQuery query);

    /**
     * 查询平台大盘筛选维度。
     *
     * @param regionId 区域编号
     * @return 筛选维度
     */
    DashboardDimensionVO selectDimensions(Integer regionId);
}
