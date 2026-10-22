package com.woha.control.system.service.impl;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.woha.control.common.annotation.DataSource;
import com.woha.control.common.enums.DataSourceType;
import com.woha.control.common.exception.ServiceException;
import com.woha.control.system.domain.external.ExternalWohaUser;
import com.woha.control.system.mapper.ExternalWohaUserMapper;
import com.woha.control.system.service.IExternalWohaUserService;

/**
 * 原业务库 Woha 用户查询服务实现
 *
 * @author luowenshen
 */
@Service
// 挂起外层主库事务，避免原业务库查询复用已绑定的主库连接。
@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class ExternalWohaUserServiceImpl implements IExternalWohaUserService
{
    private final ExternalWohaUserMapper wohaUserMapper;

    @Value("${spring.datasource.druid.slave.enabled:false}")
    private boolean slaveEnabled;

    public ExternalWohaUserServiceImpl(ExternalWohaUserMapper wohaUserMapper)
    {
        this.wohaUserMapper = wohaUserMapper;
    }

    /** 查询原业务库用户 */
    @Override
    @DataSource(DataSourceType.SLAVE)
    public ExternalWohaUser selectByUid(Long uid)
    {
        checkSlaveEnabled();
        return wohaUserMapper.selectByUid(uid);
    }

    /** 根据 ChatNo 查询原业务库用户 */
    @Override
    @DataSource(DataSourceType.SLAVE)
    public ExternalWohaUser selectByChatNo(Long chatNo)
    {
        checkSlaveEnabled();
        return wohaUserMapper.selectByChatNo(chatNo);
    }

    /** 批量查询原业务库用户 */
    @Override
    @DataSource(DataSourceType.SLAVE)
    public List<ExternalWohaUser> selectByUids(Collection<Long> uids)
    {
        checkSlaveEnabled();
        return wohaUserMapper.selectByUids(uids);
    }

    /** 校验原业务库数据源已启用 */
    private void checkSlaveEnabled()
    {
        if (!slaveEnabled)
        {
            throw new ServiceException("原业务库数据源未启用，无法查询 Woha 用户");
        }
    }
}
