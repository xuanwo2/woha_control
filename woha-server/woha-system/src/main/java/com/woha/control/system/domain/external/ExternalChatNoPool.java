package com.woha.control.system.domain.external;

import java.util.Date;
import lombok.Data;

/**
 * 外管靓号池记录。
 *
 * @author luowenshen
 */
@Data
public class ExternalChatNoPool
{
    /** 靓号池主键 */
    private Long id;
    /** 靓号 */
    private Long chatNo;
    /** 归属外管账号主键 */
    private Long accountId;
    /** 接收用户UID */
    private Long receiverUid;
    /** 接收用户Woha账号 */
    private Long receiverChatNo;
    /** 分配前用户账号 */
    private Long previousChatNo;
    /** 到期时间 */
    private Date expireTime;
    /** 状态：0可用，1已分配 */
    private String status;
}
