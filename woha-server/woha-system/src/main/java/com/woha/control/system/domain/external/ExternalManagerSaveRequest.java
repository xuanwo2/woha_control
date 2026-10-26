package com.woha.control.system.domain.external;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 外管账号保存参数
 *
 * @author luowenshen
 */
@Data
public class ExternalManagerSaveRequest
{
    /** 登录账号 */
    @NotBlank(message = "登录账号不能为空")
    private String loginName;
    /** 登录密码 */
    private String password;
    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    private String realName;
    /** 关联 Woha 用户 ChatNo */
    @NotNull(message = "关联 Woha 用户 ChatNo 不能为空")
    private Long wohaChatNo;
    /** 角色编码 */
    @NotBlank(message = "角色不能为空")
    private String roleCode;
    /** 上级外管账号主键 */
    private Long parentId;
    /** 上级外管关联 Woha 用户 ChatNo */
    private Long parentWohaChatNo;
    /** 所属大区编号 */
    @NotNull(message = "所属大区不能为空")
    private Integer regionId;
    /** 功能权限编码集合 */
    private List<String> permissions;
    /** WhatsApp账号 */
    private String whatsapp;
    /** 说明 */
    private String remark;
    /** 礼包可赠次数配置 */
    private List<ExternalGiftPackQuotaSaveRequest> giftQuotas;
}
