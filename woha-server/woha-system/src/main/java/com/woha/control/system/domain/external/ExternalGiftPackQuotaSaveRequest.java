package com.woha.control.system.domain.external;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 外管账号礼包额度保存请求。
 *
 * @author luowenshen
 */
@Data
public class ExternalGiftPackQuotaSaveRequest
{
    /** 外管账号主键 */
    @NotNull(message = "外管账号不能为空")
    private Long accountId;
    /** 礼包主键 */
    @NotNull(message = "礼包不能为空")
    private Long giftId;
    /** 可发放次数 */
    @NotNull(message = "额度不能为空")
    @Min(value = 0, message = "额度不能小于零")
    private Integer quotaCount;
}
