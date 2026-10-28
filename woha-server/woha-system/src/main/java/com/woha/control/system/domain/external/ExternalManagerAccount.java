package com.woha.control.system.domain.external;

import java.util.Date;
import lombok.Data;
import com.woha.control.common.annotation.Excel;

/**
 * 外管账号
 *
 * @author luowenshen
 */
@Data
public class ExternalManagerAccount
{
    /** 主键 */
    private Long id;
    /** 登录账号 */
    @Excel(name = "外管账号")
    private String loginName;
    /** 密码哈希值 */
    private String password;
    /** 姓名 */
    @Excel(name = "姓名")
    private String realName;
    /** 关联 Woha 用户 UID */
    @Excel(name = "关联 Woha UID")
    private Long wohaUid;
    /** 角色编码 */
    @Excel(name = "角色")
    private String roleCode;
    /** 上级外管账号主键 */
    private Long parentId;
    /** 所属大区编号 */
    private Integer regionId;
    /** 功能权限编码，逗号分隔 */
    private String permissions;
    /** 开通方式枚举编码。 */
    private String openMethod;
    /** WhatsApp账号 */
    private String whatsapp;
    /** 说明 */
    private String remark;
    /** 账号状态，0正常，1停用 */
    private String status;
    /** 删除标志，0存在，2删除 */
    private String delFlag;
    /** 连续登录失败次数 */
    private Integer loginFailCount;
    /** 最近登录失败时间 */
    private Date loginFailTime;
    /** 锁定截止时间 */
    private Date lockExpireTime;
    /** 最后登录 IP 地址 */
    private String loginIp;
    /** 最后登录时间 */
    private Date loginDate;
    /** 密码最后修改时间 */
    private Date pwdUpdateDate;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
    /** 创建者 */
    private String createBy;
    /** 更新者 */
    private String updateBy;
    /** Woha 展示账号 */
    private Long wohaChatNo;
    /** 上级外管账号 */
    @Excel(name = "上级账号")
    private String parentLoginName;
    /** 上级外管关联 Woha 用户 ChatNo */
    private Long parentWohaChatNo;
    /** 团队公会数 */
    private Integer guildCount;
    /** 本月流水 */
    private Long monthFlow;
}
