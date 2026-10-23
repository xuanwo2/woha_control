package com.woha.control.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalOperationLog;
import com.woha.control.system.mapper.ExternalManagerAccountMapper;
import com.woha.control.system.mapper.ExternalOperationLogMapper;
import com.woha.control.system.service.ExternalOperationLogService;

/**
 * 外管操作记录服务实现。
 *
 * @author luowenshen
 */
@Service
public class ExternalOperationLogServiceImpl implements ExternalOperationLogService
{
    private final ExternalOperationLogMapper logMapper;
    private final ExternalManagerAccountMapper accountMapper;

    public ExternalOperationLogServiceImpl(ExternalOperationLogMapper logMapper, ExternalManagerAccountMapper accountMapper)
    {
        this.logMapper = logMapper;
        this.accountMapper = accountMapper;
    }

    @Override
    public void record(ExternalManagerAccount account, String operationType, String target, String content)
    {
        ExternalOperationLog log = new ExternalOperationLog();
        log.setAccountId(account.getId());
        log.setSuperAdminId(findSuperAdminId(account));
        log.setRoleCode(account.getRoleCode());
        log.setOperationType(operationType);
        log.setTarget(target);
        log.setContent(content);
        logMapper.insert(log);
    }

    @Override
    public List<ExternalOperationLog> selectList(String loginName, String superAdminName, String operationType, Date beginTime, Date endTime)
    {
        return logMapper.selectList(loginName, superAdminName, operationType, beginTime, endTime);
    }

    @Override
    public void deleteSubordinateLogs(String loginName)
    {
        logMapper.deleteSubordinateLogs(loginName);
    }

    /** 向上查找所属超级管理员。 */
    private Long findSuperAdminId(ExternalManagerAccount account)
    {
        ExternalManagerAccount current = account;
        while (current != null)
        {
            if ("SUPER_ADMIN".equals(current.getRoleCode())) return current.getId();
            current = current.getParentId() == null ? null : accountMapper.selectAccountById(current.getParentId());
        }
        return null;
    }
}
