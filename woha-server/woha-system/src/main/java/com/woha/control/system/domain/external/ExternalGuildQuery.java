package com.woha.control.system.domain.external;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 外管公会查询条件
 *
 * @author luowenshen
 */
@Data
public class ExternalGuildQuery
{
    /** 可见负责人 Woha 用户 UID 集合 */
    private List<Long> visibleWohaUids;
    /** 公会编号或名称 */
    private String keyword;
    /** 负责人 Woha 用户 UID */
    private Long responsibleWohaUid;
    /** 创建来源 */
    private Integer createSource;
    /** 统计开始日期 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    /** 统计结束日期 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    /** 所选结束日所属周的开始日期 */
    private LocalDate weekStartDate;
    /** 所选结束日所属月的开始日期 */
    private LocalDate monthStartDate;
}
