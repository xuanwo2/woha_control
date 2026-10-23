package com.woha.control.system.domain.external;

import lombok.Data;

/**
 * 外管公会流水汇总数据
 *
 * @author luowenshen
 */
@Data
public class ExternalGuildFlowSummary
{
    /** 范围流水 */
    private Long rangeFlow;
    /** 周流水 */
    private Long weekFlow;
    /** 月流水 */
    private Long monthFlow;
    /** 历史总流水 */
    private Long totalFlow;
}
