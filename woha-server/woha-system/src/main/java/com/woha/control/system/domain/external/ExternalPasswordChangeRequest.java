package com.woha.control.system.domain.external;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 外管密码修改参数
 *
 * @author luowenshen
 */
@Data
public class ExternalPasswordChangeRequest
{
    /** 原密码 */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;
    /** 新密码 */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
