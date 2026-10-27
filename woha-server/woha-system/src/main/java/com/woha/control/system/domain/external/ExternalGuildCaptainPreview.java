package com.woha.control.system.domain.external;

import lombok.Data;

/**
 * 外管新建公会会长预览信息
 *
 * @author luowenshen
 */
@Data
public class ExternalGuildCaptainPreview
{
    /** 会长用户 UID */
    private Long uid;
    /** 会长 Woha 展示账号 */
    private Long chatNo;
    /** 会长昵称 */
    private String nickname;
    /** 会长所属大区编号 */
    private Integer regionId;
    /** 是否已注册 */
    private Boolean registered;
    /** 是否属于当前大区 */
    private Boolean inCurrentRegion;
    /** 是否已加入公会 */
    private Boolean joinedFamily;
}
