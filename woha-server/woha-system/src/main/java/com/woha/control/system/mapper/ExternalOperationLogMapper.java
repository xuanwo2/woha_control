package com.woha.control.system.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.woha.control.system.domain.external.ExternalOperationLog;

/**
 * 外管操作记录数据访问。
 *
 * @author luowenshen
 */
@Mapper
public interface ExternalOperationLogMapper
{
    /** 新增操作记录。 */
    int insert(ExternalOperationLog log);
    /** 查询操作记录。 */
    List<ExternalOperationLog> selectList(@Param("loginName") String loginName, @Param("superAdminName") String superAdminName,
            @Param("operationType") String operationType, @Param("beginTime") Date beginTime, @Param("endTime") Date endTime);
    /** 查询账号最近操作记录 */
    List<ExternalOperationLog> selectLatestByAccountId(@Param("accountId") Long accountId, @Param("limit") int limit);
    /** 删除指定账号新增下级记录。 */
    int deleteSubordinateLogs(@Param("target") String target);
    /** 统计当天指定类型操作次数。 */
    int countToday(@Param("accountId") Long accountId, @Param("operationType") String operationType);
}
