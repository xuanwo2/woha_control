package com.woha.control.system.service.impl;

import org.springframework.stereotype.Service;
import com.woha.control.common.annotation.DataSource;
import com.woha.control.common.enums.DataSourceType;
import com.woha.control.system.domain.external.ExternalWohaUser;
import com.woha.control.system.mapper.ExternalBanBusinessMapper;

/**
 * 原业务库账号封禁服务。
 *
 * @author luowenshen
 */
@Service
public class ExternalBanBusinessService
{
    private final ExternalBanBusinessMapper mapper;
    public ExternalBanBusinessService(ExternalBanBusinessMapper mapper) { this.mapper = mapper; }
    /** 查询封禁对象与所属负责人。 */
    @DataSource(DataSourceType.SLAVE)
    public ExternalWohaUser selectUser(Long chatNo) { return mapper.selectUser(chatNo); }
    /** 查询公会负责人。 */
    @DataSource(DataSourceType.SLAVE)
    public Long selectResponsibleUid(Long uid) { return mapper.selectResponsibleUid(uid); }
    /** 写入封禁记录。 */
    @DataSource(DataSourceType.SLAVE)
    public void ban(Long uid, Long chatNo, Integer dayNum, String reason, Integer regionId) { mapper.insertBan(uid, chatNo, dayNum, reason, regionId); }
    /** 解除封禁。 */
    @DataSource(DataSourceType.SLAVE)
    public void release(Long chatNo) { mapper.releaseBan(chatNo); }
}
