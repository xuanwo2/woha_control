package com.woha.control.system.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.woha.control.common.annotation.DataSource;
import com.woha.control.common.enums.DataSourceType;
import com.woha.control.common.exception.ServiceException;
import com.woha.control.system.domain.external.ExternalGuildCreateRequest;
import com.woha.control.system.domain.external.ExternalGuildFlowSummary;
import com.woha.control.system.domain.external.ExternalGuildQuery;
import com.woha.control.system.domain.external.ExternalGuildVO;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalWohaUser;
import com.woha.control.system.mapper.ExternalGuildMapper;
import com.woha.control.system.service.IExternalGuildService;

/**
 * 外管公会服务实现
 *
 * @author luowenshen
 */
@Service
public class ExternalGuildServiceImpl implements IExternalGuildService
{
    private final ExternalGuildMapper guildMapper;

    @Value("${spring.datasource.druid.slave.enabled:false}")
    private boolean slaveEnabled;

    public ExternalGuildServiceImpl(ExternalGuildMapper guildMapper)
    {
        this.guildMapper = guildMapper;
    }

    @Override
    @DataSource(DataSourceType.SLAVE)
    @Transactional(rollbackFor = Exception.class)
    public Long createGuild(ExternalGuildCreateRequest request, ExternalManagerAccount responsible)
    {
        checkSlaveEnabled();
        ExternalWohaUser captain = guildMapper.selectWohaUserForUpdate(request.getCaptainChatNo());
        if (captain == null)
        {
            throw new ServiceException("会长 Woha ID不存在");
        }
        if (!responsible.getRegionId().equals(captain.getRegionId()))
        {
            throw new ServiceException("会长与负责人不在同一大区");
        }
        if (guildMapper.countFamilyMemberByUid(captain.getUid()) > 0)
        {
            throw new ServiceException("该用户已加入公会，不能创建公会");
        }
        Long familyId = guildMapper.selectFamilyId();
        if (familyId == null)
        {
            throw new ServiceException("生成公会编号失败");
        }
        guildMapper.insertFamily(familyId, captain.getUid(), responsible.getWohaUid(), responsible.getRegionId(), request);
        guildMapper.insertCaptainMember(familyId, captain.getUid(), responsible.getRegionId());
        return familyId;
    }

    @Override
    @DataSource(DataSourceType.SLAVE)
    public List<ExternalGuildVO> selectGuildList(ExternalGuildQuery query)
    {
        checkSlaveEnabled();
        normalizeQueryDates(query);
        return query.getVisibleWohaUids() == null || query.getVisibleWohaUids().isEmpty() ? List.of() : guildMapper.selectGuildList(query);
    }

    @Override
    @DataSource(DataSourceType.SLAVE)
    public ExternalGuildFlowSummary selectGuildFlowSummary(ExternalGuildQuery query)
    {
        checkSlaveEnabled();
        normalizeQueryDates(query);
        if (query.getVisibleWohaUids() == null || query.getVisibleWohaUids().isEmpty())
        {
            return new ExternalGuildFlowSummary();
        }
        return guildMapper.selectGuildFlowSummary(query);
    }

    /** 统一统计日期口径 */
    private void normalizeQueryDates(ExternalGuildQuery query)
    {
        LocalDate endDate = query.getEndDate() == null ? LocalDate.now() : query.getEndDate();
        LocalDate startDate = query.getStartDate() == null ? endDate : query.getStartDate();
        if (startDate.isAfter(endDate))
        {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setWeekStartDate(endDate.with(DayOfWeek.MONDAY));
        query.setMonthStartDate(endDate.withDayOfMonth(1));
    }

    /** 校验原业务库数据源已启用 */
    private void checkSlaveEnabled()
    {
        if (!slaveEnabled)
        {
            throw new ServiceException("原业务库数据源未启用，无法操作公会数据");
        }
    }
}
