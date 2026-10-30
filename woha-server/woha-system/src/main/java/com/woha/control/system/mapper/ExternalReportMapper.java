package com.woha.control.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.woha.control.system.domain.external.ExternalReport;

/**
 * 外管举报记录数据访问。
 *
 * @author luowenshen
 */
@Mapper
public interface ExternalReportMapper
{
    /** 新增举报记录。 */
    int insert(ExternalReport report);

    /** 查询指定外管账号提交的举报记录。 */
    List<ExternalReport> selectByReporterUid(@Param("reporterUid") Long reporterUid, @Param("regionId") Integer regionId);
}
