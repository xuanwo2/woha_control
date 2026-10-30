package com.woha.control.system.service.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.woha.control.common.annotation.DataSource;
import com.woha.control.common.enums.DataSourceType;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalManagerRole;
import com.woha.control.system.domain.external.ExternalTeamMetric;
import com.woha.control.system.mapper.ExternalManagerAccountMapper;
import com.woha.control.system.mapper.ExternalTeamMetricMapper;
import com.woha.control.system.service.IExternalManagerTeamService;

/**
 * 外管团队服务实现
 *
 * @author luowenshen
 */
@Service
public class ExternalManagerTeamServiceImpl implements IExternalManagerTeamService
{
    private final ExternalManagerAccountMapper accountMapper;
    private final ExternalTeamMetricMapper metricMapper;

    public ExternalManagerTeamServiceImpl(ExternalManagerAccountMapper accountMapper, ExternalTeamMetricMapper metricMapper)
    { this.accountMapper = accountMapper; this.metricMapper = metricMapper; }

    @Override
    @DataSource(DataSourceType.SLAVE)
    public List<Long> resolveVisibleAccountIds(ExternalManagerAccount current)
    {
        List<ExternalManagerAccount> accounts = accountMapper.selectAccountSummariesByRegion(current.getRegionId());
        if (ExternalManagerRole.SUPER_ADMIN.name().equals(current.getRoleCode())) return accounts.stream().map(ExternalManagerAccount::getId).collect(Collectors.toList());
        Map<Long, List<Long>> children = new HashMap<>();
        accounts.forEach(item -> { if (item.getParentId() != null) children.computeIfAbsent(item.getParentId(), key -> new ArrayList<>()).add(item.getId()); });
        List<Long> result = new ArrayList<>(); ArrayDeque<Long> queue = new ArrayDeque<>(); queue.add(current.getId());
        while (!queue.isEmpty()) { Long id = queue.removeFirst(); result.add(id); queue.addAll(children.getOrDefault(id, List.of())); }
        return result;
    }

    @Override
    @DataSource(DataSourceType.SLAVE)
    public List<ExternalManagerAccount> selectTeamMembers(List<Long> ids, String loginName, String roleCode, String status)
    { return ids.isEmpty() ? List.of() : accountMapper.selectTeamAccountList(ids, loginName, roleCode, status); }

    @Override
    @DataSource(DataSourceType.SLAVE)
    public List<ExternalManagerAccount> selectDirectSubordinateCounts(List<ExternalManagerAccount> accounts)
    {
        List<Long> parentIds = accounts.stream().map(ExternalManagerAccount::getId).collect(Collectors.toList());
        return parentIds.isEmpty() ? List.of() : accountMapper.selectDirectSubordinateCounts(parentIds);
    }

    @Override
    @DataSource(DataSourceType.SLAVE)
    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    public List<ExternalTeamMetric> selectMetrics(List<ExternalManagerAccount> accounts)
    {
        Collection<Long> uids = accounts.stream().map(ExternalManagerAccount::getWohaUid).distinct().collect(Collectors.toList());
        return uids.isEmpty() ? List.of() : metricMapper.selectMetricsByWohaUids(uids);
    }

    @Override
    @DataSource(DataSourceType.SLAVE)
    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    public int countAnchors(List<ExternalManagerAccount> accounts)
    {
        Collection<Long> uids = accounts.stream().map(ExternalManagerAccount::getWohaUid).distinct().collect(Collectors.toList());
        return uids.isEmpty() ? 0 : metricMapper.countAnchorsByWohaUids(uids);
    }
}
