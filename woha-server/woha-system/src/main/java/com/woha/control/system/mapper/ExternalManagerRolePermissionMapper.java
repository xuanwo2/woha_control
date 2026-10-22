package com.woha.control.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.woha.control.system.domain.external.ExternalManagerRolePermission;

/**
 * 外管角色默认权限数据访问
 *
 * @author luowenshen
 */
@Mapper
public interface ExternalManagerRolePermissionMapper
{
    /** 查询大区角色默认权限 */
    List<ExternalManagerRolePermission> selectByRegionId(@Param("regionId") Integer regionId);

    /** 删除大区角色默认权限 */
    int deleteByRegionId(@Param("regionId") Integer regionId);

    /** 批量新增角色默认权限 */
    int insertBatch(@Param("items") List<ExternalManagerRolePermission> items);
}
