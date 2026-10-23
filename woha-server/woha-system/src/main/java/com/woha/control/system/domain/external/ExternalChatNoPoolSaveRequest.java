package com.woha.control.system.domain.external;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 外管靓号池配置请求。
 *
 * @author luowenshen
 */
@Data
public class ExternalChatNoPoolSaveRequest
{
    /** 外管账号主键 */
    @NotNull(message = "外管账号不能为空")
    private Long accountId;
    /** 靓号 */
    @NotNull(message = "靓号不能为空")
    private Long chatNo;
}
