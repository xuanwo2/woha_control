package com.woha.control.system.service.impl;

import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.woha.control.common.exception.ServiceException;
import com.woha.control.system.domain.external.ExternalChatNoAssignRequest;
import com.woha.control.system.domain.external.ExternalChatNoPool;
import com.woha.control.system.domain.external.ExternalChatNoPoolSaveRequest;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalManagerPermission;
import com.woha.control.system.domain.external.ExternalWohaUser;
import com.woha.control.system.mapper.ExternalChatNoMapper;
import com.woha.control.system.service.IExternalChatNoService;
import com.woha.control.system.service.IExternalWohaUserService;

/**
 * 外管靓号服务实现。
 *
 * @author luowenshen
 */
@Service
public class ExternalChatNoServiceImpl implements IExternalChatNoService
{
    private final ExternalChatNoMapper chatNoMapper;
    private final IExternalWohaUserService wohaUserService;
    private final ExternalChatNoBusinessService businessService;
    private final ExternalIchatUserCacheService ichatUserCacheService;

    public ExternalChatNoServiceImpl(ExternalChatNoMapper chatNoMapper, IExternalWohaUserService wohaUserService,
            ExternalChatNoBusinessService businessService, ExternalIchatUserCacheService ichatUserCacheService)
    {
        this.chatNoMapper = chatNoMapper;
        this.wohaUserService = wohaUserService;
        this.businessService = businessService;
        this.ichatUserCacheService = ichatUserCacheService;
    }

    @Override
    public List<ExternalChatNoPool> selectPoolList(Long accountId)
    {
        return chatNoMapper.selectPoolList(accountId);
    }

    @Override
    public void addPool(ExternalChatNoPoolSaveRequest request)
    {
        if (request.getChatNo() <= 0)
        {
            throw new ServiceException("靓号不合法");
        }
        if (wohaUserService.selectByChatNo(request.getChatNo()) != null)
        {
            throw new ServiceException("靓号已被业务用户占用");
        }
        ExternalChatNoPool pool = new ExternalChatNoPool();
        pool.setAccountId(request.getAccountId());
        pool.setChatNo(request.getChatNo());
        try
        {
            chatNoMapper.insertPool(pool);
        }
        catch (DuplicateKeyException e)
        {
            throw new ServiceException("靓号已划拨至其他外管账号");
        }
    }

    @Override
    public void removeAvailablePool(Long accountId, Long chatNo)
    {
        if (chatNoMapper.deleteAvailablePool(accountId, chatNo) != 1)
        {
            throw new ServiceException("仅可回收当前账号下未分配的靓号");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(ExternalManagerAccount account, ExternalChatNoAssignRequest request)
    {
        ensurePermission(account);
        validateDayNum(request.getDayNum());
        ExternalChatNoPool pool = chatNoMapper.selectPoolForUpdate(request.getChatNo());
        if (pool == null || !account.getId().equals(pool.getAccountId()) || !"0".equals(pool.getStatus()))
        {
            throw new ServiceException("靓号不存在或不可分配");
        }
        ExternalWohaUser receiver = businessService.assign(request.getReceiverChatNo(), request.getChatNo(), request.getDayNum(), account.getRegionId());
        ichatUserCacheService.evictUserCache(receiver.getUid());
        if (chatNoMapper.assignPool(request.getChatNo(), receiver.getUid(), receiver.getChatNo(), request.getDayNum()) != 1)
        {
            throw new ServiceException("靓号状态更新失败");
        }
    }

    /** 校验外管分配权限。 */
    private void ensurePermission(ExternalManagerAccount account)
    {
        if (account.getPermissions() == null || !List.of(account.getPermissions().split(","))
                .contains(ExternalManagerPermission.ASSIGN_CHAT_NO.name()))
        {
            throw new ServiceException("当前账号没有分配靓号权限");
        }
    }

    /** 校验固定有效期。 */
    private void validateDayNum(Integer dayNum)
    {
        if (dayNum == null || (dayNum != 30 && dayNum != 90 && dayNum != 180))
        {
            throw new ServiceException("靓号有效期仅支持30、90或180天");
        }
    }
}
