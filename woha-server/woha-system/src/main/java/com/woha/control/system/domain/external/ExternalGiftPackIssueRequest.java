package com.woha.control.system.domain.external;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 外管礼包发放请求。
 *
 * @author luowenshen
 */
@Data
public class ExternalGiftPackIssueRequest
{
    /** 接收用户Woha账号 */
    @NotNull(message = "接收用户账号不能为空")
    private Long receiverChatNo;
    /** 礼包主键 */
    @NotNull(message = "礼包不能为空")
    private Long giftId;
}
