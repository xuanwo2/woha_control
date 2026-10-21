package com.woha.business.bi.service;

import java.util.List;
import com.woha.business.bi.domain.InviteGrowthDashboardVO;
import com.woha.business.bi.domain.InviteGrowthQuery;
import com.woha.business.bi.domain.InviteQualityVO;
import com.woha.business.bi.domain.UserGrowthDimensionVO;

/**
 * 邀请增长看板服务。
 *
 * @author luowenshen
 */
public interface IInviteGrowthDashboardService
{
    /** 查询邀请增长看板聚合数据。 */
    InviteGrowthDashboardVO selectDashboard(InviteGrowthQuery query);

    /** 查询邀请人质量分层数据。 */
    List<InviteQualityVO> selectInviteQuality(InviteGrowthQuery query);

    /** 查询邀请增长筛选维度。 */
    UserGrowthDimensionVO selectDimensions(Integer regionId);
}
