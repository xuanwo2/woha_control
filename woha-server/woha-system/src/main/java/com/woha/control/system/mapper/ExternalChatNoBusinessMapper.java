package com.woha.control.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.woha.control.system.domain.external.ExternalWohaUser;

/**
 * 原业务库靓号数据访问。
 *
 * @author luowenshen
 */
@Mapper
public interface ExternalChatNoBusinessMapper
{
    /** 锁定接收用户。 */
    ExternalWohaUser selectUserForUpdate(@Param("chatNo") Long chatNo);

    /** 锁定用户当前有效靓号记录。 */
    Long selectActiveCuteNoRecordForUpdate(@Param("uid") Long uid);

    /** 查询靓号是否已被业务账号占用。 */
    int countChatNoInUse(@Param("chatNo") Long chatNo);

    /** 更新用户展示账号。 */
    int updateUserChatNo(@Param("uid") Long uid, @Param("chatNo") Long chatNo);

    /** 更新账号表展示账号。 */
    int updateAccountChatNo(@Param("uid") Long uid, @Param("chatNo") Long chatNo);

    /** 新增靓号分配记录。 */
    int insertCuteNoRecord(@Param("uid") Long uid, @Param("previousNo") Long previousNo,
            @Param("cuteNo") Long cuteNo, @Param("dayNum") Integer dayNum, @Param("regionId") Integer regionId);

}
