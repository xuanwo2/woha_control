package com.woha.control.system.domain.external;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 外管登录参数
 *
 * @author luowenshen
 */
@Data
public class ExternalLoginRequest
{
    /** 登录账号 */
    @NotBlank(message = "登录账号不能为空")
    private String loginName;
    /** 登录密码 */
    @NotBlank(message = "登录密码不能为空")
    private String password;
}
