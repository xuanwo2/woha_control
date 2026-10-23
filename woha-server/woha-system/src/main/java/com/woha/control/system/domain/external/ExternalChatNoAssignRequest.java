package com.woha.control.system.domain.external;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 外管靓号分配请求。
 *
 * @author luowenshen
 */
@Data
public class ExternalChatNoAssignRequest
{
    /** 靓号 */
    @NotNull(message = "靓号不能为空")
    private Long chatNo;
    /** 接收用户Woha账号 */
    @NotNull(message = "接收用户账号不能为空")
    private Long receiverChatNo;
    /** 有效天数 */
    @NotNull(message = "有效期不能为空")
    private Integer dayNum;
}
