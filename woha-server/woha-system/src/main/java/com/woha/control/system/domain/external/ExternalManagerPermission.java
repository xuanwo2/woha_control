package com.woha.control.system.domain.external;

/**
 * 外管功能权限
 *
 * @author luowenshen
 */
public enum ExternalManagerPermission
{
    /** 查看团队与公会 */
    VIEW_TEAM_GUILD,
    /** 查看公会流水 */
    VIEW_GUILD_FLOW,
    /** 新增下级 */
    CREATE_SUBORDINATE,
    /** 新建公会 */
    CREATE_GUILD,
    /** 赠送礼包 */
    SEND_GIFT_PACK,
    /** 分配靓号 */
    ASSIGN_CHAT_NO,
    /** 执行封禁 */
    EXECUTE_BAN,
    /** 提交举报 */
    SUBMIT_REPORT,
    /** 数据导出 */
    EXPORT_DATA
}
