package com.woha.control.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.woha.control.system.domain.external.ExternalGiftPack;
import com.woha.control.system.domain.external.ExternalGiftPackIssue;
import com.woha.control.system.domain.external.ExternalGiftPackItem;
import com.woha.control.system.domain.external.ExternalGiftPackQuota;

/**
 * 外管礼包数据访问。
 *
 * @author luowenshen
 */
@Mapper
public interface ExternalGiftPackMapper
{
    /** 查询礼包列表 */
    List<ExternalGiftPack> selectGiftPackList(@Param("status") String status, @Param("regionId") Integer regionId);
    /** 查询礼包详情 */
    ExternalGiftPack selectGiftPackById(@Param("id") Long id);
    /** 查询礼包内容 */
    List<ExternalGiftPackItem> selectItemsByGiftId(@Param("giftId") Long giftId);
    /** 批量查询礼包内容 */
    List<ExternalGiftPackItem> selectItemsByGiftIds(@Param("giftIds") List<Long> giftIds);
    /** 新增礼包 */
    int insertGiftPack(ExternalGiftPack giftPack);
    /** 更新礼包 */
    int updateGiftPack(ExternalGiftPack giftPack);
    /** 删除礼包内容 */
    int deleteItemsByGiftId(@Param("giftId") Long giftId);
    /** 批量新增礼包内容 */
    int insertItems(@Param("items") List<ExternalGiftPackItem> items);
    /** 查询账号额度 */
    List<ExternalGiftPackQuota> selectQuotaList(@Param("accountId") Long accountId);
    /** 保存账号额度 */
    int saveQuota(ExternalGiftPackQuota quota);
    /** 原子扣减账号额度 */
    int consumeQuota(@Param("accountId") Long accountId, @Param("giftId") Long giftId);
    /** 查询用户已领取次数 */
    int countReceiverIssues(@Param("giftId") Long giftId, @Param("receiverUid") Long receiverUid);
    /** 保存发放记录 */
    int insertIssue(@Param("requestNo") String requestNo, @Param("giftId") Long giftId,
            @Param("accountId") Long accountId, @Param("receiverUid") Long receiverUid,
            @Param("regionId") Integer regionId, @Param("grantNo") Integer grantNo,
            @Param("contentSnapshot") String contentSnapshot);
    /** 查询发放记录 */
    List<ExternalGiftPackIssue> selectIssueList(@Param("accountId") Long accountId);
}
