package com.woha.control.system.domain.external;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 外管创建公会请求
 *
 * @author luowenshen
 */
@Data
public class ExternalGuildCreateRequest
{
    /** 会长 Woha ID */
    @NotNull(message = "会长 Woha ID不能为空")
    private Long captainChatNo;

    /** 公会名称 */
    @NotBlank(message = "公会名称不能为空")
    @Size(max = 30, message = "公会名称不能超过30个字符")
    private String familyName;

    /** 公会封面地址 */
    @Size(max = 500, message = "公会封面地址长度超限")
    private String familyLogo;

    /** 会长 WhatsApp */
    @Size(max = 64, message = "会长 WhatsApp长度超限")
    private String whatsapp;

    /** 负责人外管账号主键 */
    @NotNull(message = "负责人不能为空")
    private Long responsibleAccountId;
}
