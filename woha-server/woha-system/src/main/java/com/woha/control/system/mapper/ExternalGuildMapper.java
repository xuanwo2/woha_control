package com.woha.control.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.woha.control.system.domain.external.ExternalGuildCreateRequest;
import com.woha.control.system.domain.external.ExternalGuildCaptainPreview;
import com.woha.control.system.domain.external.ExternalGuildFlowSummary;
import com.woha.control.system.domain.external.ExternalGuildQuery;
import com.woha.control.system.domain.external.ExternalGuildVO;
import com.woha.control.system.domain.external.ExternalWohaUser;

/**
 * 外管公会原业务数据访问
 *
 * @author luowenshen
 */
@Mapper
public interface ExternalGuildMapper
{
    /** 查询原业务用户并锁定 */
    ExternalWohaUser selectWohaUserForUpdate(@Param("chatNo") Long chatNo);

    /** 查询新建公会会长预览信息 */
    ExternalGuildCaptainPreview selectCaptainPreview(@Param("chatNo") Long chatNo);

    /** 查询用户是否已加入公会 */
    int countFamilyMemberByUid(@Param("uid") Long uid);

    /** 查询公会号是否存在 */
    int countFamilyByNo(@Param("familyNo") Long familyNo);

    /** 创建公会 */
    int insertFamily(@Param("familyNo") Long familyNo, @Param("captainUid") Long captainUid, @Param("regionId") Integer regionId,
            @Param("request") ExternalGuildCreateRequest request);

    /** 查询当前连接最近生成的公会主键 */
    Long selectLastInsertId();

    /** 创建会长成员关系 */
    int insertCaptainMember(@Param("familyId") Long familyId, @Param("captainUid") Long captainUid,
            @Param("regionId") Integer regionId);

    /** 创建公会与负责 BD 的归属关系 */
    int insertResponsibleRelation(@Param("familyId") Long familyId, @Param("bdUserId") Long bdUserId,
            @Param("regionId") Integer regionId);

    /** 查询公会列表 */
    List<ExternalGuildVO> selectGuildList(ExternalGuildQuery query);

    /** 查询流水汇总 */
    ExternalGuildFlowSummary selectGuildFlowSummary(ExternalGuildQuery query);

}
