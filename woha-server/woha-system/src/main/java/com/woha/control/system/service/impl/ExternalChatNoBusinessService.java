package com.woha.control.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.woha.control.common.annotation.DataSource;
import com.woha.control.common.enums.DataSourceType;
import com.woha.control.common.exception.ServiceException;
import com.woha.control.system.domain.external.ExternalWohaUser;
import com.woha.control.system.mapper.ExternalChatNoBusinessMapper;

/**
 * 原业务库靓号分配服务。
 *
 * @author luowenshen
 */
@Service
public class ExternalChatNoBusinessService
{
    private final ExternalChatNoBusinessMapper businessMapper;

    public ExternalChatNoBusinessService(ExternalChatNoBusinessMapper businessMapper)
    {
        this.businessMapper = businessMapper;
    }

    /** 在原业务库分配靓号。 */
    @DataSource(DataSourceType.SLAVE)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public ExternalWohaUser assign(Long receiverChatNo, Long cuteNo, Integer dayNum, Integer expectedRegionId)
    {
        ExternalWohaUser receiver = businessMapper.selectUserForUpdate(receiverChatNo);
        if (receiver == null || !expectedRegionId.equals(receiver.getRegionId()))
        {
            throw new ServiceException("接收用户不存在或不属于当前大区");
        }
        if (businessMapper.selectActiveCuteNoRecordForUpdate(receiver.getUid()) != null)
        {
            throw new ServiceException("接收用户已持有有效靓号");
        }
        if (businessMapper.countChatNoInUse(cuteNo) > 0)
        {
            throw new ServiceException("靓号已被业务账号占用");
        }
        if (businessMapper.updateUserChatNo(receiver.getUid(), cuteNo) != 1
                || businessMapper.updateAccountChatNo(receiver.getUid(), cuteNo) != 1)
        {
            throw new ServiceException("更新原业务账号失败");
        }
        businessMapper.insertCuteNoRecord(receiver.getUid(), receiver.getChatNo(), cuteNo, dayNum, receiver.getRegionId());
        return receiver;
    }

}
