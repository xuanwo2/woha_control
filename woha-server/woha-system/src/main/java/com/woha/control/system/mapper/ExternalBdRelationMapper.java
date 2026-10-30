package com.woha.control.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 外管账号与 BD 体系关联数据访问
 *
 * @author luowenshen
 */
@Mapper
public interface ExternalBdRelationMapper
{
    /** 根据 Woha 用户 UID 查询 BDLeader 主键 */
    Integer selectBdLeaderIdByUid(@Param("uid") Long uid);

    /** 创建 BDLeader */
    int insertBdLeader(@Param("uid") Long uid, @Param("regionId") Integer regionId, @Param("remark") String remark);

    /** 根据 Woha 用户 UID 查询 BD 用户主键 */
    Long selectBdUserIdByUid(@Param("uid") Long uid);

    /** 根据 Woha 用户 UID 查询归属 BDLeader 主键 */
    Integer selectBdUserLeaderIdByUid(@Param("uid") Long uid);

    /** 创建 BD 用户 */
    int insertBdUser(@Param("uid") Long uid, @Param("bdLeaderId") Integer bdLeaderId,
            @Param("type") Integer type, @Param("regionId") Integer regionId, @Param("remark") String remark);
}
