package com.woha.control.system.service;

import java.util.List;
import com.woha.control.system.domain.external.ExternalGiftPack;
import com.woha.control.system.domain.external.ExternalGiftPackIssue;
import com.woha.control.system.domain.external.ExternalGiftPackIssueRequest;
import com.woha.control.system.domain.external.ExternalGiftPackQuota;
import com.woha.control.system.domain.external.ExternalGiftPackQuotaSaveRequest;
import com.woha.control.system.domain.external.ExternalManagerAccount;

/**
 * 外管礼包服务。
 *
 * @author luowenshen
 */
public interface IExternalGiftPackService
{
    /** 查询礼包列表 */
    List<ExternalGiftPack> selectGiftPackList(String status);
    /** 保存礼包 */
    void saveGiftPack(ExternalGiftPack giftPack, String operator);
    /** 查询额度列表 */
    List<ExternalGiftPackQuota> selectQuotaList(Long accountId);
    /** 保存额度 */
    void saveQuota(ExternalGiftPackQuotaSaveRequest request);
    /** 发放礼包 */
    void issueGiftPack(ExternalManagerAccount account, ExternalGiftPackIssueRequest request);
    /** 查询发放记录 */
    List<ExternalGiftPackIssue> selectIssueList(Long accountId);
}
