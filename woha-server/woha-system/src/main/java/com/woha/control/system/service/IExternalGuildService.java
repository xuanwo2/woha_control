package com.woha.control.system.service;

import java.util.List;
import com.woha.control.system.domain.external.ExternalGuildCreateRequest;
import com.woha.control.system.domain.external.ExternalGuildFlowSummary;
import com.woha.control.system.domain.external.ExternalGuildQuery;
import com.woha.control.system.domain.external.ExternalGuildVO;
import com.woha.control.system.domain.external.ExternalManagerAccount;

/**
 * 外管公会服务
 *
 * @author luowenshen
 */
public interface IExternalGuildService
{
    /** 创建公会 */
    Long createGuild(ExternalGuildCreateRequest request, ExternalManagerAccount responsible);

    /** 查询公会列表 */
    List<ExternalGuildVO> selectGuildList(ExternalGuildQuery query);

    /** 查询公会流水汇总 */
    ExternalGuildFlowSummary selectGuildFlowSummary(ExternalGuildQuery query);
}
