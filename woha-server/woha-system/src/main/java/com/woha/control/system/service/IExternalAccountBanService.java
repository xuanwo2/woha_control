package com.woha.control.system.service;

import com.woha.control.system.domain.external.ExternalAccountBanRequest;
import com.woha.control.system.domain.external.ExternalManagerAccount;

/**
 * 外管账号封禁服务。
 *
 * @author luowenshen
 */
public interface IExternalAccountBanService
{
    /** 封禁团队公会成员。 */
    void ban(ExternalManagerAccount account, ExternalAccountBanRequest request);
    /** 提前解除团队公会成员封禁。 */
    void release(ExternalManagerAccount account, Long chatNo);
}
