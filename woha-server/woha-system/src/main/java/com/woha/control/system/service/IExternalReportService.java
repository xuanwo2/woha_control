package com.woha.control.system.service;

import java.util.List;
import com.woha.control.system.domain.external.ExternalManagerAccount;
import com.woha.control.system.domain.external.ExternalReport;
import com.woha.control.system.domain.external.ExternalReportSubmitRequest;

/**
 * 外管举报服务。
 *
 * @author luowenshen
 */
public interface IExternalReportService
{
    /** 提交举报。 */
    Long submit(ExternalManagerAccount account, ExternalReportSubmitRequest request);

    /** 查询当前账号提交的举报记录。 */
    List<ExternalReport> selectMyReports(ExternalManagerAccount account);
}
