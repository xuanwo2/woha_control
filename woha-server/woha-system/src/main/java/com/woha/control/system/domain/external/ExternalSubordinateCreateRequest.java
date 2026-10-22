package com.woha.control.system.domain.external;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 外管新增下级参数
 *
 * @author luowenshen
 */
@Data
public class ExternalSubordinateCreateRequest
{
    /** 登录账号 */
    @NotBlank(message = "登录账号不能为空")
    private String loginName;
    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    private String realName;
    /** 关联Woha用户ChatNo */
    @NotNull(message = "关联Woha用户ChatNo不能为空")
    private Long wohaChatNo;
    /** 下级角色编码 */
    @NotBlank(message = "角色不能为空")
    private String roleCode;
    /** WhatsApp账号 */
    private String whatsapp;
    /** 说明 */
    private String remark;
}
