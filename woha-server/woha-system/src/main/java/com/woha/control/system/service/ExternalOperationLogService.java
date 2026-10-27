package com.woha.control.system.service;

import java.util.Date;
import java.util.List;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalOperationLog;

/**
 * 外管操作记录服务。
 *
 * @author luowenshen
 */
public interface ExternalOperationLogService
{
    /** 记录外管操作。 */
    void record(ExternalManagerAccount account, String operationType, String target, String content);
    /** 查询操作记录。 */
    List<ExternalOperationLog> selectList(String loginName, String superAdminName, String operationType, Date beginTime, Date endTime);
    /** 查询账号最近操作记录。 */
    List<ExternalOperationLog> selectLatestByAccountId(Long accountId, int limit);
    /** 统计账号当天指定类型操作次数。 */
    int countToday(Long accountId, String operationType);
    /** 删除新增指定下级的操作记录。 */
    void deleteSubordinateLogs(String loginName);
}
