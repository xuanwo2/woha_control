package com.woha.control.system.domain.external;

import java.util.List;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 外管角色默认权限保存参数
 *
 * @author luowenshen
 */
@Data
public class ExternalManagerRolePermissionRequest
{
    /** 所属大区编号 */
    @NotNull(message = "所属大区不能为空")
    private Integer regionId;
    /** 超级管理员权限集合 */
    private List<String> superAdminPermissions;
    /** 商务负责人权限集合 */
    private List<String> bdLeaderPermissions;
    /** 商务人员权限集合 */
    private List<String> bdPermissions;
}
