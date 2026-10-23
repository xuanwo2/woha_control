package com.woha.control.system.domain.external;

import java.util.Date;
import lombok.Data;

/**
 * 外管操作记录。
 *
 * @author luowenshen
 */
@Data
public class ExternalOperationLog
{
    /** 操作记录主键 */
    private Long id;
    /** 外管账号主键 */
    private Long accountId;
    /** 所属超级管理员账号主键 */
    private Long superAdminId;
    /** 外管账号 */
    private String loginName;
    /** 外管角色 */
    private String roleCode;
    /** 所属超级管理员 */
    private String superAdminName;
    /** 操作类型 */
    private String operationType;
    /** 操作对象 */
    private String target;
    /** 操作内容 */
    private String content;
    /** 操作时间 */
    private Date createTime;
}
