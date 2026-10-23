package com.woha.control.system.service.impl;

import java.util.UUID;
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

    /** 在原业务库发放金币、装扮和待激活VIP卡 */
    @DataSource(DataSourceType.SLAVE)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void issue(Long accountId, Long receiverUid, Integer regionId, ExternalGiftPack giftPack)
    {
        String remark = "外管礼包：" + giftPack.getGiftName();
        String requestNo = UUID.randomUUID().toString().replace("-", "");
        if (giftPack.getGoldAmount() != null && giftPack.getGoldAmount() > 0)
        {
            if (businessMapper.increaseGold(receiverUid, giftPack.getGoldAmount()) != 1)
            {
                throw new ServiceException("接收用户钱包不存在");
            }
            Long balance = businessMapper.selectGoldBalance(receiverUid);
            businessMapper.insertGoldLog(receiverUid, giftPack.getGoldAmount(), balance, requestNo, regionId, remark);
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
