package com.woha.business.bi.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.woha.business.bi.domain.TrackingAppRelease;
import com.woha.business.bi.domain.TrackingQualitySourceEventStat;
import com.woha.business.bi.mapper.TrackingQualitySourceMapper;
import com.woha.business.bi.service.ITrackingQualitySourceService;
import com.woha.business.common.annotation.DataSource;
import com.woha.business.common.enums.DataSourceType;

/**
 * 业务库埋点质量数据服务实现。
 *
 * @author luowenshen
 */
@Service
@DataSource(DataSourceType.SLAVE)
public class TrackingQualitySourceServiceImpl implements ITrackingQualitySourceService
{
    /** 业务库埋点质量数据访问层。 */
    private final TrackingQualitySourceMapper sourceMapper;

    public TrackingQualitySourceServiceImpl(TrackingQualitySourceMapper sourceMapper)
    {
        this.sourceMapper = sourceMapper;
    }

    @Override
    public Long selectTodayReports(Integer regionId)
    {
        return sourceMapper.selectTodayReports(regionId);
    }

    @Override
    public Map<String, Object> selectRecentTotals(Integer regionId)
    {
        return sourceMapper.selectRecentTotals(regionId);
    }

    @Override
    public List<TrackingQualitySourceEventStat> selectEventStats(Integer regionId)
    {
        return sourceMapper.selectEventStats(regionId);
    }

    @Override
    public List<TrackingAppRelease> selectRecentReleases()
    {
        return sourceMapper.selectRecentReleases();
    }

    @Override
    public List<String> selectVersionEvents(String versionNo, Integer regionId)
    {
        return sourceMapper.selectVersionEvents(versionNo, regionId);
    }
}
