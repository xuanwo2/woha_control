package com.woha.business.bi.service;

import java.util.List;
import com.woha.business.bi.domain.PlayDashboardQuery;
import com.woha.business.bi.domain.PlayDashboardVO;
import com.woha.business.bi.domain.PlayGameOverviewVO;

/**
 * 玩法看板服务。
 *
 * @author luowenshen
 */
public interface IPlayDashboardService
{
    /** 查询玩法看板聚合数据。 */
    PlayDashboardVO selectDashboard(PlayDashboardQuery query);

    /** 查询玩法总览导出数据。 */
    List<PlayGameOverviewVO> selectGameOverview(PlayDashboardQuery query);

    /** 查询玩法看板可选地区。 */
    List<String> selectCountries(Integer regionId);
}
