package com.woha.control.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.woha.control.system.domain.external.ExternalBusinessProp;

/**
 * 原业务库礼包发放数据访问。
 *
 * @author luowenshen
 */
@Mapper
public interface ExternalGiftBusinessMapper
{
    /** 查询有效装扮 */
    ExternalBusinessProp selectValidProp(@Param("id") Long id);
    /** 批量查询有效装扮 */
    List<ExternalBusinessProp> selectValidProps(@Param("ids") List<Long> ids);
    /** 批量查询装扮 */
    List<ExternalBusinessProp> selectPropsByIds(@Param("ids") List<Long> ids);
    /** 增加金币余额 */
    int increaseGold(@Param("uid") Long uid, @Param("amount") Long amount);
    /** 查询金币余额 */
    Long selectGoldBalance(@Param("uid") Long uid);
    /** 写入金币流水 */
    int insertGoldLog(@Param("uid") Long uid, @Param("amount") Long amount, @Param("balance") Long balance,
            @Param("orderId") String orderId, @Param("regionId") Integer regionId, @Param("remark") String remark);
    /** 写入装扮 */
    int insertPropGive(@Param("uid") Long uid, @Param("prop") ExternalBusinessProp prop,
            @Param("dayNum") Integer dayNum, @Param("quantity") Integer quantity,
            @Param("regionId") Integer regionId, @Param("remark") String remark);
    /** 写入待激活VIP卡 */
    int insertVipCard(@Param("requestNo") String requestNo, @Param("issuerId") Long issuerId,
            @Param("receiverUid") Long receiverUid, @Param("vipLevel") Integer vipLevel,
            @Param("dayNum") Integer dayNum, @Param("regionId") Integer regionId);
}
