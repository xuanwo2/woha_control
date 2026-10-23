package com.woha.control.system.domain.external;

import lombok.Data;

/**
 * 外管账号礼包额度。
 *
 * @author luowenshen
 */
@Data
public class ExternalGiftPackQuota
{
    /** 外管账号主键 */
    private Long accountId;
    /** 外管账号名称 */
    private String loginName;
    /** 礼包主键 */
    private Long giftId;
    /** 礼包名称 */
    private String giftName;
    /** 可发放次数 */
    private Integer quotaCount;
    /** 已发放次数 */
    private Integer usedCount;
}
