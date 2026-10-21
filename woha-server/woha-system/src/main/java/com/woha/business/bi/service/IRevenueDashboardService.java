package com.woha.business.bi.service;

import java.util.List;
import com.woha.business.bi.domain.RevenueDashboardQuery;
import com.woha.business.bi.domain.RevenueDashboardVO;

/**
 * 营收看板服务。
 *
 * @author luowenshen
 */
public interface IRevenueDashboardService
{
    /** 查询营收看板聚合数据。 */
    RevenueDashboardVO selectDashboard(RevenueDashboardQuery query);

    /** 查询营收看板可选地区。 */
    List<String> selectCountries(Integer regionId);
}
