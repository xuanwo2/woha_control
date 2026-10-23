package com.woha.control.system.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.woha.control.common.exception.ServiceException;
import com.woha.control.system.domain.external.ExternalAccountBanRequest;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalManagerPermission;
import com.woha.control.system.domain.external.ExternalWohaUser;
import com.woha.control.system.mapper.ExternalBanBusinessMapper;
import com.woha.control.system.mapper.ExternalOperationLogMapper;
import com.woha.control.system.service.IExternalAccountBanService;
import com.woha.control.system.service.IExternalManagerTeamService;
import com.woha.control.system.service.ExternalOperationLogService;

/**
 * 外管账号封禁服务实现。
 *
 * @author luowenshen
 */
@Service
public class ExternalAccountBanServiceImpl implements IExternalAccountBanService
{
    private final ExternalBanBusinessMapper banMapper; private final ExternalBanBusinessService businessService; private final IExternalManagerTeamService teamService;
    private final ExternalOperationLogMapper logMapper; private final ExternalOperationLogService logService;
    public ExternalAccountBanServiceImpl(ExternalBanBusinessMapper banMapper, ExternalBanBusinessService businessService, IExternalManagerTeamService teamService, ExternalOperationLogMapper logMapper, ExternalOperationLogService logService) { this.banMapper=banMapper; this.businessService=businessService; this.teamService=teamService; this.logMapper=logMapper; this.logService=logService; }
    @Override
    public void ban(ExternalManagerAccount account, ExternalAccountBanRequest request) { validate(account, request.getChatNo()); if (logMapper.countToday(account.getId(), "BAN") >= 10) throw new ServiceException("今日封禁次数已达上限"); ExternalWohaUser user=businessService.selectUser(request.getChatNo()); businessService.ban(user.getUid(),user.getChatNo(),request.getDayNum(),request.getReason(),account.getRegionId()); logService.record(account,"BAN",String.valueOf(request.getChatNo()),request.getReason()+"，"+request.getDayNum()+"天"); }
    @Override
    public void release(ExternalManagerAccount account, Long chatNo) { validate(account, chatNo); businessService.release(chatNo); logService.record(account,"UNBAN",String.valueOf(chatNo),"提前解除封禁"); }
    private void validate(ExternalManagerAccount account, Long chatNo) { if (account.getPermissions()==null || !List.of(account.getPermissions().split(",")).contains(ExternalManagerPermission.EXECUTE_BAN.name())) throw new ServiceException("当前账号没有执行封禁权限"); ExternalWohaUser user=businessService.selectUser(chatNo); if (user==null || !account.getRegionId().equals(user.getRegionId())) throw new ServiceException("用户不存在或不属于当前大区"); Long responsibleUid=businessService.selectResponsibleUid(user.getUid()); List<Long> visible=teamService.selectTeamMembers(teamService.resolveVisibleAccountIds(account),null,null,"0").stream().map(ExternalManagerAccount::getWohaUid).toList(); if (responsibleUid==null || !visible.contains(responsibleUid)) throw new ServiceException("仅可封禁本团队公会成员"); }
}
