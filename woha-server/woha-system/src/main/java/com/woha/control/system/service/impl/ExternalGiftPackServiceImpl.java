package com.woha.control.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.woha.control.common.annotation.DataSource;
import com.woha.control.common.enums.DataSourceType;
import com.woha.control.common.exception.ServiceException;
import com.woha.control.system.domain.external.ExternalBusinessProp;
import com.woha.control.system.domain.external.ExternalGiftPack;
import com.woha.control.system.domain.external.ExternalGiftPackIssue;
import com.woha.control.system.domain.external.ExternalGiftPackIssueRequest;
import com.woha.control.system.domain.external.ExternalGiftPackItem;
import com.woha.control.system.domain.external.ExternalGiftPackQuota;
import com.woha.control.system.domain.external.ExternalGiftPackQuotaSaveRequest;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalManagerPermission;
import com.woha.control.system.domain.external.ExternalWohaUser;
import com.woha.control.system.mapper.ExternalGiftPackMapper;
import com.woha.control.system.service.IExternalGiftPackService;
import com.woha.control.system.service.IExternalWohaUserService;

/**
 * 外管礼包服务实现。
 *
 * @author luowenshen
 */
@Service
@DataSource(DataSourceType.SLAVE)
public class ExternalGiftPackServiceImpl implements IExternalGiftPackService
{
    private final ExternalGiftPackMapper giftPackMapper;
    private final IExternalWohaUserService wohaUserService;
    private final ExternalGiftBusinessService businessService;

    public ExternalGiftPackServiceImpl(ExternalGiftPackMapper giftPackMapper, IExternalWohaUserService wohaUserService,
            ExternalGiftBusinessService businessService)
    {
        this.giftPackMapper = giftPackMapper;
        this.wohaUserService = wohaUserService;
        this.businessService = businessService;
    }

    @Override
    public List<ExternalGiftPack> selectGiftPackList(String status, Integer regionId)
    {
        List<ExternalGiftPack> list = giftPackMapper.selectGiftPackList(status, regionId);
        if (list.isEmpty())
        {
            return list;
        }
        List<Long> giftIds = list.stream().map(ExternalGiftPack::getId).collect(Collectors.toList());
        List<ExternalGiftPackItem> items = giftPackMapper.selectItemsByGiftIds(giftIds);
        Map<Long, List<ExternalGiftPackItem>> itemsByGiftId = items.stream()
                .collect(Collectors.groupingBy(ExternalGiftPackItem::getGiftId));
        List<Long> propIds = items.stream().filter(item -> "PROP".equals(item.getItemType()))
                .map(ExternalGiftPackItem::getPropId).distinct().collect(Collectors.toList());
        Map<Long, ExternalBusinessProp> propsById = propIds.isEmpty() ? Map.of() : businessService.selectProps(propIds)
                .stream().collect(Collectors.toMap(ExternalBusinessProp::getId, prop -> prop));
        for (ExternalGiftPack giftPack : list)
        {
            List<ExternalGiftPackItem> giftItems = itemsByGiftId.getOrDefault(giftPack.getId(), new ArrayList<>());
            for (ExternalGiftPackItem item : giftItems)
            {
                if ("PROP".equals(item.getItemType()))
                {
                    ExternalBusinessProp prop = propsById.get(item.getPropId());
                    if (prop != null)
                    {
                        item.setPropType(prop.getPropType());
                    }
                }
            }
            giftPack.setItems(giftItems);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGiftPack(ExternalGiftPack giftPack, String operator)
    {
        validateGiftPack(giftPack);
        giftPack.setCreateBy(operator);
        giftPack.setUpdateBy(operator);
        if (giftPack.getId() == null)
        {
            giftPackMapper.insertGiftPack(giftPack);
        }
        else
        {
            giftPackMapper.updateGiftPack(giftPack);
            giftPackMapper.deleteItemsByGiftId(giftPack.getId());
        }
        for (int index = 0; index < giftPack.getItems().size(); index++)
        {
            ExternalGiftPackItem item = giftPack.getItems().get(index);
            item.setGiftId(giftPack.getId());
            item.setSortNo(index);
        }
        giftPackMapper.insertItems(giftPack.getItems());
    }

    @Override
    public List<ExternalGiftPackQuota> selectQuotaList(Long accountId)
    {
        return giftPackMapper.selectQuotaList(accountId);
    }

    @Override
    public void saveQuota(ExternalGiftPackQuotaSaveRequest request, Integer regionId)
    {
        validateGiftPackRegion(request.getGiftId(), regionId);
        ExternalGiftPackQuota quota = new ExternalGiftPackQuota();
        quota.setAccountId(request.getAccountId());
        quota.setGiftId(request.getGiftId());
        quota.setQuotaCount(request.getQuotaCount());
        giftPackMapper.saveQuota(quota);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issueGiftPack(ExternalManagerAccount account, ExternalGiftPackIssueRequest request)
    {
        if (!hasPermission(account, ExternalManagerPermission.SEND_GIFT_PACK))
        {
            throw new ServiceException("当前账号没有赠送礼包权限");
        }
        ExternalGiftPack giftPack = giftPackMapper.selectGiftPackById(request.getGiftId());
        if (giftPack == null || !"0".equals(giftPack.getStatus()))
        {
            throw new ServiceException("礼包不存在或已停用");
        }
        if (!account.getRegionId().equals(giftPack.getRegionId()))
        {
            throw new ServiceException("不能发放其他大区的礼包");
        }
        giftPack.setItems(giftPackMapper.selectItemsByGiftId(giftPack.getId()));
        ExternalWohaUser receiver = wohaUserService.selectByChatNo(request.getReceiverChatNo(), account.getRegionId());
        if (receiver == null)
        {
            throw new ServiceException("接收用户不存在或不属于当前大区");
        }
        int grantedCount = giftPackMapper.countReceiverIssues(giftPack.getId(), receiver.getUid());
        if (grantedCount >= giftPack.getReceiveLimit())
        {
            throw new ServiceException("该用户已达到此礼包领取上限");
        }
        if (giftPackMapper.consumeQuota(account.getId(), giftPack.getId()) != 1)
        {
            throw new ServiceException("当前礼包发放额度不足");
        }
        String requestNo = UUID.randomUUID().toString().replace("-", "");
        giftPackMapper.insertIssue(requestNo, giftPack.getId(), account.getId(), receiver.getUid(), account.getRegionId(),
                grantedCount + 1, buildSnapshot(giftPack));
        businessService.issue(account.getId(), receiver.getUid(), account.getRegionId(), requestNo, giftPack);
    }

    @Override
    public List<ExternalGiftPackIssue> selectIssueList(Long accountId)
    {
        return giftPackMapper.selectIssueList(accountId);
    }

    /** 校验礼包配置 */
    private void validateGiftPack(ExternalGiftPack giftPack)
    {
        if (giftPack.getRegionId() == null || giftPack.getRegionId() < 1 || giftPack.getRegionId() > 7)
        {
            throw new ServiceException("所属大区不合法");
        }
        if (!"0".equals(giftPack.getStatus()) && !"1".equals(giftPack.getStatus()))
        {
            throw new ServiceException("礼包状态不合法");
        }
        if (giftPack.getGiftName() == null || giftPack.getGiftName().isBlank() || giftPack.getGoldAmount() == null
                || giftPack.getGoldAmount() < 0 || giftPack.getReceiveLimit() == null || giftPack.getReceiveLimit() < 1
                || giftPack.getItems() == null || giftPack.getItems().isEmpty())
        {
            throw new ServiceException("礼包配置不完整");
        }
        List<Long> propIds = new ArrayList<>();
        for (ExternalGiftPackItem item : giftPack.getItems())
        {
            if (item.getDayNum() == null || item.getDayNum() < 1 || item.getQuantity() == null || item.getQuantity() < 1)
            {
                throw new ServiceException("礼包内容有效天数或数量不合法");
            }
            if ("PROP".equals(item.getItemType()) && item.getPropId() != null) { propIds.add(item.getPropId()); continue; }
            if ("VIP".equals(item.getItemType()) && (item.getVipLevel() != null
                    && (item.getVipLevel() == 1000 || item.getVipLevel() == 2000 || item.getVipLevel() == 3000))) continue;
            throw new ServiceException("礼包内容类型或内容配置不合法");
        }
        businessService.validateProps(propIds.stream().distinct().collect(java.util.stream.Collectors.toList()));
    }

    /** 校验礼包归属大区。 */
    private void validateGiftPackRegion(Long giftId, Integer regionId)
    {
        ExternalGiftPack giftPack = giftPackMapper.selectGiftPackById(giftId);
        if (giftPack == null || !regionId.equals(giftPack.getRegionId()) || !"0".equals(giftPack.getStatus()))
        {
            throw new ServiceException("礼包与外管账号所属大区不一致");
        }
    }

    /** 判断功能权限 */
    private boolean hasPermission(ExternalManagerAccount account, ExternalManagerPermission permission)
    {
        return account.getPermissions() != null && List.of(account.getPermissions().split(",")).contains(permission.name());
    }

    /** 构建发放内容快照 */
    private String buildSnapshot(ExternalGiftPack giftPack)
    {
        List<String> values = new ArrayList<>();
        if (giftPack.getGoldAmount() > 0) values.add("金币" + giftPack.getGoldAmount());
        for (ExternalGiftPackItem item : giftPack.getItems())
        {
            values.add("VIP".equals(item.getItemType()) ? "VIP" + item.getVipLevel() + " " + item.getDayNum() + "天"
                    : "装扮" + item.getPropId() + " " + item.getDayNum() + "天");
        }
        return String.join("、", values);
    }
}
