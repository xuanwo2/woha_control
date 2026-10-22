package com.woha.control.system.domain.external;

import lombok.Data;

/**
 * 外管团队原业务统计数据
 *
 * @author luowenshen
 */
@Data
public class ExternalTeamMetric
{
    /** Woha用户UID */
    private Long wohaUid;
    /** 团队公会数 */
    private Integer guildCount;
    /** 今日流水 */
    private Long dayFlow;
    /** 本周流水 */
    private Long weekFlow;
    /** 本月流水 */
    private Long monthFlow;
    /** 累计流水 */
    private Long totalFlow;
}
