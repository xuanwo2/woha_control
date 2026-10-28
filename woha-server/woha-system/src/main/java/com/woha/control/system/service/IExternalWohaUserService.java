package com.woha.control.system.service;

import java.util.Collection;
import java.util.List;
import com.woha.control.common.annotation.DataSource;
import com.woha.control.common.enums.DataSourceType;
import com.woha.control.system.domain.external.ExternalWohaUser;

/**
 * 原业务库 Woha 用户查询服务
 *
 * @author luowenshen
 */
public interface IExternalWohaUserService
{
    /** 根据 UID 查询 Woha 用户 */
    @DataSource(DataSourceType.SLAVE)
    ExternalWohaUser selectByUid(Long uid);

    /** 根据 ChatNo 和大区查询 Woha 用户 */
    @DataSource(DataSourceType.SLAVE)
    ExternalWohaUser selectByChatNo(Long chatNo, Integer regionId);

    /** 批量查询 Woha 用户 */
    @DataSource(DataSourceType.SLAVE)
    List<ExternalWohaUser> selectByUids(Collection<Long> uids);
}
