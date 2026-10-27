package com.woha.control.system.service;

import java.util.List;
import com.woha.control.system.domain.external.ExternalChatNoAssignRequest;
import com.woha.control.system.domain.external.ExternalChatNoPool;
import com.woha.control.system.domain.external.ExternalChatNoPoolSaveRequest;
import com.woha.control.system.domain.external.ExternalManagerAccount;

/**
 * 外管靓号服务。
 *
 * @author luowenshen
 */
public interface IExternalChatNoService
{
    /** 查询账号靓号池。 */
    List<ExternalChatNoPool> selectPoolList(Long accountId);
    /** 统计账号靓号总数。 */
    int countPoolByAccountId(Long accountId);
    /** 统计账号可用靓号数。 */
    int countAvailablePoolByAccountId(Long accountId);
    /** 管理后台向外管账号划拨靓号。 */
    void addPool(ExternalChatNoPoolSaveRequest request);
    /** 管理后台回收未分配靓号。 */
    void removeAvailablePool(Long accountId, Long chatNo);
    /** 外管分配靓号。 */
    void assign(ExternalManagerAccount account, ExternalChatNoAssignRequest request);
}
