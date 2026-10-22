package com.woha.control.system.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.woha.control.system.domain.external.ExternalTeamMetric;

/**
 * 外管团队原业务统计数据访问
 *
 * @author luowenshen
 */
@Mapper
public interface ExternalTeamMetricMapper
{
    /** 按Woha用户批量统计团队公会及流水 */
    List<ExternalTeamMetric> selectMetricsByWohaUids(@Param("wohaUids") Collection<Long> wohaUids);
}
