package com.woha.control.system.service.impl;

import java.util.List;
import com.alibaba.fastjson2.JSON;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.woha.control.common.annotation.DataSource;
import com.woha.control.common.enums.DataSourceType;
import com.woha.control.common.exception.ServiceException;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalReport;
import com.woha.control.system.domain.external.ExternalReportSubmitRequest;
import com.woha.control.system.domain.external.ExternalWohaUser;
import com.woha.control.system.mapper.ExternalReportMapper;
import com.woha.control.system.service.IExternalReportService;
import com.woha.control.system.service.IExternalWohaUserService;

/**
 * 外管举报服务实现。
 *
 * @author luowenshen
 */
@Service
@DataSource(DataSourceType.SLAVE)
public class ExternalReportServiceImpl implements IExternalReportService
{
    private final ExternalReportMapper reportMapper;
    private final IExternalWohaUserService wohaUserService;

    public ExternalReportServiceImpl(ExternalReportMapper reportMapper, IExternalWohaUserService wohaUserService)
    {
        this.reportMapper = reportMapper;
        this.wohaUserService = wohaUserService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(ExternalManagerAccount account, ExternalReportSubmitRequest request)
    {
        ExternalWohaUser target = wohaUserService.selectByChatNo(request.getTargetChatNo(), account.getRegionId());
        if (target == null)
        {
            throw new ServiceException("被举报用户不存在或不属于当前大区");
        }
        if (request.getEvidenceUrls() != null && request.getEvidenceUrls().stream().anyMatch(item -> item == null || item.isBlank()))
        {
            throw new ServiceException("证据图片路径不能为空");
        }
        ExternalReport report = new ExternalReport();
        if (account.getWohaUid() == null)
        {
            throw new ServiceException("当前外管账号未关联Woha用户");
        }
        report.setReporterAccountId(account.getWohaUid());
        report.setTargetUid(target.getUid());
        report.setTargetChatNo(target.getChatNo());
        report.setViolationType(request.getViolationType());
        report.setDescription(request.getDescription());
        report.setEvidenceUrls(request.getEvidenceUrls() == null ? null : JSON.toJSONString(request.getEvidenceUrls()));
        report.setStatus("0");
        report.setRegionId(account.getRegionId());
        reportMapper.insert(report);
        return report.getId();
    }

    @Override
    public List<ExternalReport> selectMyReports(ExternalManagerAccount account)
    {
        return reportMapper.selectByReporterUid(account.getWohaUid(), account.getRegionId());
    }
}
