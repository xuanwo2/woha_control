package com.woha.control.system.domain.external;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 外管账号封禁请求。
 *
 * @author luowenshen
 */
@Data
public class ExternalAccountBanRequest
{
    /** 目标用户Woha账号 */
    @NotNull(message = "用户Woha账号不能为空")
    private Long chatNo;
    /** 封禁天数 */
    @NotNull(message = "封禁天数不能为空")
    @Min(value = 1, message = "封禁天数不能小于1天")
    @Max(value = 7, message = "封禁天数不能大于7天")
    private Integer dayNum;
    /** 封禁原因 */
    @NotBlank(message = "封禁原因不能为空")
    private String reason;
}
