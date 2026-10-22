package com.woha.control.system.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.woha.control.system.domain.external.ExternalWohaUser;

/**
 * 原业务库 Woha 用户数据访问
 *
 * @author luowenshen
 */
@Mapper
public interface ExternalWohaUserMapper
{
    /** 根据 UID 查询 Woha 用户 */
    ExternalWohaUser selectByUid(@Param("uid") Long uid);

    /** 根据 ChatNo 查询 Woha 用户 */
    ExternalWohaUser selectByChatNo(@Param("chatNo") Long chatNo);

    /** 批量查询 Woha 用户 */
    List<ExternalWohaUser> selectByUids(@Param("uids") Collection<Long> uids);
}
