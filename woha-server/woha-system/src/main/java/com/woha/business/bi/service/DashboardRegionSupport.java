package com.woha.business.bi.service;

import java.util.Set;
import com.woha.business.common.exception.ServiceException;

/**
 * 看板区域参数校验工具，与业务服务 Region 枚举保持一致。
 *
 * @author luowenshen
 */
public final class DashboardRegionSupport
{
    /** 默认区域编号，对应东南亚。 */
    private static final Integer DEFAULT_REGION_ID = 2;

    /** 业务服务支持的区域编号集合。 */
    private static final Set<Integer> REGION_IDS = Set.of(1, 2, 3, 4, 5, 6, 7);

    private DashboardRegionSupport()
    {
    }

    /**
     * 解析并校验区域编号。
     *
     * @param regionId 请求区域编号
     * @return 有效区域编号
     */
    public static Integer resolve(Integer regionId)
    {
        Integer actualRegionId = regionId == null ? DEFAULT_REGION_ID : regionId;
        if (!REGION_IDS.contains(actualRegionId))
        {
            throw new ServiceException("区域编号不正确");
        }
        return actualRegionId;
    }
}
