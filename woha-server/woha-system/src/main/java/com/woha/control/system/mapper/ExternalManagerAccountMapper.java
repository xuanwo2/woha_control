package com.woha.control.system.mapper;

import java.util.List;
import java.util.Collection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.woha.control.system.domain.external.ExternalManagerAccount;

/**
 * 外管账号数据访问
 *
 * @author luowenshen
 */
@Mapper
public interface ExternalManagerAccountMapper
{
    /** 查询外管账号列表 */
    List<ExternalManagerAccount> selectAccountList(@Param("loginName") String loginName,
            @Param("wohaUid") Long wohaUid, @Param("roleCode") String roleCode,
            @Param("regionId") Integer regionId, @Param("status") String status,
            @Param("parentLoginName") String parentLoginName, @Param("openMethod") String openMethod);

    /** 查询指定大区的外管账号摘要 */
    List<ExternalManagerAccount> selectAccountSummariesByRegion(@Param("regionId") Integer regionId);

    /** 按可见账号范围查询团队成员 */
    List<ExternalManagerAccount> selectTeamAccountList(@Param("ids") Collection<Long> ids,
            @Param("loginName") String loginName, @Param("roleCode") String roleCode, @Param("status") String status);

    /** 根据主键查询外管账号 */
    ExternalManagerAccount selectAccountById(@Param("id") Long id);

    /** 根据登录账号查询外管账号 */
    ExternalManagerAccount selectAccountByLoginName(@Param("loginName") String loginName);

    /** 根据登录账号锁定查询外管账号 */
    ExternalManagerAccount selectAccountByLoginNameForUpdate(@Param("loginName") String loginName);

    /** 新增外管账号 */
    int insertAccount(ExternalManagerAccount account);

    /** 修改外管账号 */
    int updateAccount(ExternalManagerAccount account);

    /** 修改账号状态 */
    int updateAccountStatus(@Param("id") Long id, @Param("status") String status);

    /** 逻辑删除账号 */
    int deleteAccount(@Param("id") Long id);

    /** 查询角色有效账号数量 */
    int countActiveAccountByRole(@Param("roleCode") String roleCode);

    /** 更新登录失败状态 */
    int updateLoginFailure(@Param("id") Long id, @Param("loginFailCount") int loginFailCount,
            @Param("lockExpireTime") java.util.Date lockExpireTime);

    /** 清除登录失败状态并更新登录信息 */
    int updateLoginSuccess(@Param("id") Long id, @Param("loginIp") String loginIp);

    /** 更新密码 */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /** 重置密码并要求首次登录修改 */
    int resetPassword(@Param("id") Long id, @Param("password") String password);
}
