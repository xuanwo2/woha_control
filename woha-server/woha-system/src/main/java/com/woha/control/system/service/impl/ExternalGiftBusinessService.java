package com.woha.control.system.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import com.woha.control.common.annotation.DataSource;
import com.woha.control.common.enums.DataSourceType;
import com.woha.control.common.exception.ServiceException;
import com.woha.control.system.domain.external.ExternalBusinessProp;
import com.woha.control.system.domain.external.ExternalGiftPack;
import com.woha.control.system.domain.external.ExternalGiftPackItem;
import com.woha.control.system.mapper.ExternalGiftBusinessMapper;

/**
 * 原业务库礼包发放服务。
 *
 * @author luowenshen
 */
@Service
public class ExternalGiftBusinessService
{
    private final ExternalGiftBusinessMapper businessMapper;

    public ExternalGiftBusinessService(ExternalGiftBusinessMapper businessMapper)
    {
        this.businessMapper = businessMapper;
    }

    /** 批量校验装扮是否存在且可用。 */
    @DataSource(DataSourceType.SLAVE)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void validateProps(List<Long> propIds)
    {
        if (propIds.isEmpty())
        {
            return;
        }
        Set<Long> validIds = businessMapper.selectValidProps(propIds).stream().map(ExternalBusinessProp::getId).collect(Collectors.toSet());
        if (!validIds.containsAll(propIds))
        {
            throw new ServiceException("礼包配置的装扮不存在或不可用");
        }
    }

    /** 批量查询装扮信息。 */
    @DataSource(DataSourceType.SLAVE)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<ExternalBusinessProp> selectProps(List<Long> propIds)
    {
        if (propIds.isEmpty())
        {
            return List.of();
        }
        return businessMapper.selectPropsByIds(propIds);
    }

    /** 在原业务库发放金币、装扮和待激活VIP卡 */
    @DataSource(DataSourceType.SLAVE)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void issue(Long accountId, Long receiverUid, Integer regionId, String requestNo, ExternalGiftPack giftPack)
    {
        String remark = "外管礼包：" + giftPack.getGiftName();
        if (giftPack.getGoldAmount() != null && giftPack.getGoldAmount() > 0)
        {
            if (businessMapper.increaseGold(receiverUid, giftPack.getGoldAmount()) != 1)
            {
                throw new ServiceException("接收用户钱包不存在");
            }
            Long balance = businessMapper.selectGoldBalance(receiverUid);
            businessMapper.insertGoldLog(receiverUid, giftPack.getGoldAmount(), balance, requestNo, regionId, remark);
            if (businessMapper.insertGoldBill(receiverUid, giftPack.getGoldAmount(), requestNo, regionId, remark) != 1)
            {
                throw new ServiceException("接收用户不存在");
            }
            businessMapper.insertSysGoldRecord(receiverUid, giftPack.getGoldAmount(), accountId, requestNo, regionId, remark);
        }
        for (ExternalGiftPackItem item : giftPack.getItems())
        {
            if ("PROP".equals(item.getItemType()))
            {
                ExternalBusinessProp prop = businessMapper.selectValidProp(item.getPropId());
                if (prop == null)
                {
                    throw new ServiceException("礼包配置的装扮不存在或不可用");
                }
                businessMapper.insertPropGive(receiverUid, prop, item.getDayNum(), item.getQuantity(), regionId, remark);
            }
            else
            {
                businessMapper.insertVipCard(requestNo + item.getSortNo(), accountId, receiverUid, item.getVipLevel(),
                        item.getDayNum(), regionId);
            }
        }
    }
}
