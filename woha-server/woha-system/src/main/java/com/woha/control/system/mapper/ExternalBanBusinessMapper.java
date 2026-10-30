package com.woha.control.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.woha.control.system.domain.external.ExternalWohaUser;

/**
 * 原业务库账号封禁数据访问。
 *
 * @author luowenshen
 */
@Mapper
public interface ExternalBanBusinessMapper
{
    /** 查询用户。 */
    ExternalWohaUser selectUser(@Param("chatNo") Long chatNo);
    /** 查询用户所属公会负责人。 */
    Long selectResponsibleUid(@Param("uid") Long uid);
    /** 查询当天封禁次数。 */
    int countTodayBan(@Param("operatorUid") Long operatorUid);
    /** 新增封禁记录。 */
    int insertBan(@Param("uid") Long uid, @Param("chatNo") Long chatNo, @Param("dayNum") Integer dayNum, @Param("reason") String reason,
            @Param("evidenceUrls") String evidenceUrls, @Param("regionId") Integer regionId);
    /** 解除当前封禁。 */
    int releaseBan(@Param("chatNo") Long chatNo);
}
