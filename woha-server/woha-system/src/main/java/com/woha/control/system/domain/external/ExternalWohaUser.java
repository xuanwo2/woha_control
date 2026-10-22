package com.woha.control.system.domain.external;

import lombok.Data;

/**
 * 原业务库 Woha 用户摘要
 *
 * @author luowenshen
 */
@Data
public class ExternalWohaUser
{
    /** 用户 UID */
    private Long uid;
    /** Woha 展示账号 */
    private Long chatNo;
    /** 所属大区编号 */
    private Integer regionId;
}
