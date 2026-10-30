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
    /** 查询VIP专属装扮 */
    List<ExternalBusinessProp> selectVipPrivilegeProps(@Param("vipLevel") Integer vipLevel,
            @Param("regionId") Integer regionId);
    /** 查询VIP配置数量 */
    int countVipConfig(@Param("vipLevel") Integer vipLevel, @Param("regionId") Integer regionId);
    /** 增加金币余额 */
    int increaseGold(@Param("uid") Long uid, @Param("amount") Long amount);
    /** 查询金币余额 */
    Long selectGoldBalance(@Param("uid") Long uid);
    /** 写入金币流水 */
    int insertGoldLog(@Param("uid") Long uid, @Param("amount") Long amount, @Param("balance") Long balance,
            @Param("orderId") String orderId, @Param("regionId") Integer regionId, @Param("remark") String remark);
    /** 写入金币账单 */
    int insertGoldBill(@Param("uid") Long uid, @Param("amount") Long amount, @Param("orderId") String orderId,
            @Param("regionId") Integer regionId, @Param("remark") String remark);
    /** 写入系统赠送金币记录 */
    int insertSysGoldRecord(@Param("uid") Long uid, @Param("amount") Long amount, @Param("adminId") Long adminId,
            @Param("orderId") String orderId, @Param("regionId") Integer regionId, @Param("reason") String reason);
    /** 写入装扮 */
    int insertPropGive(@Param("uid") Long uid, @Param("prop") ExternalBusinessProp prop,
            @Param("dayNum") Integer dayNum, @Param("quantity") Integer quantity,
            @Param("regionId") Integer regionId, @Param("remark") String remark);
    /** 直接开通或续期用户VIP */
    int grantVip(@Param("receiverUid") Long receiverUid, @Param("vipLevel") Integer vipLevel,
            @Param("dayNum") Integer dayNum, @Param("regionId") Integer regionId);
    /** 写入用户VIP开通记录 */
    int insertVipRecord(@Param("receiverUid") Long receiverUid, @Param("vipLevel") Integer vipLevel,
            @Param("dayNum") Integer dayNum, @Param("orderId") String orderId, @Param("regionId") Integer regionId);
}
