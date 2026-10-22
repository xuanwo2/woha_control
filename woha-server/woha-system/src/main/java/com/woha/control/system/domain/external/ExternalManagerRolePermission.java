package com.woha.control.system.domain.external;

import lombok.Data;

/**
 * 外管角色默认权限
 *
 * @author luowenshen
 */
@Data
public class ExternalManagerRolePermission
{
    /** 大区编号 */
    private Integer regionId;
    /** 角色编码 */
    private String roleCode;
    /** 功能权限编码 */
    private String permissionCode;
}
