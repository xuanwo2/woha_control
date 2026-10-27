package com.woha.control.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.woha.control.system.domain.external.ExternalChatNoPool;

/**
 * 外管靓号池数据访问。
 *
 * @author luowenshen
 */
@Mapper
public interface ExternalChatNoMapper
{
    /** 查询靓号池 */
    List<ExternalChatNoPool> selectPoolList(@Param("accountId") Long accountId);
    /** 统计账号靓号总数 */
    int countPoolByAccountId(@Param("accountId") Long accountId);
    /** 统计账号可用靓号数 */
    int countAvailablePoolByAccountId(@Param("accountId") Long accountId);
    /** 锁定靓号池记录 */
    ExternalChatNoPool selectPoolForUpdate(@Param("chatNo") Long chatNo);
    /** 新增靓号池记录 */
    int insertPool(ExternalChatNoPool pool);
    /** 回收未分配靓号。 */
    int deleteAvailablePool(@Param("accountId") Long accountId, @Param("chatNo") Long chatNo);
    /** 原子分配靓号 */
    int assignPool(@Param("chatNo") Long chatNo, @Param("receiverUid") Long receiverUid,
            @Param("previousChatNo") Long previousChatNo, @Param("dayNum") Integer dayNum);
}
