package com.woha.control.system.service;

import java.util.List;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalTeamMetric;

/**
 * 外管团队服务
 *
 * @author luowenshen
 */
public interface IExternalManagerTeamService
{
    /** 计算当前账号可见的团队账号主键 */
    List<Long> resolveVisibleAccountIds(ExternalManagerAccount current);
    /** 查询团队成员 */
    List<ExternalManagerAccount> selectTeamMembers(List<Long> ids, String loginName, String roleCode, String status);
    /** 批量查询直属下级数量 */
    List<ExternalManagerAccount> selectDirectSubordinateCounts(List<ExternalManagerAccount> accounts);
    /** 批量查询团队原业务指标 */
    List<ExternalTeamMetric> selectMetrics(List<ExternalManagerAccount> accounts);
    /** 统计团队公会主播数 */
    int countAnchors(List<ExternalManagerAccount> accounts);
}
