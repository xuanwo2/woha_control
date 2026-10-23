package com.woha.control.system.domain.external;

import java.util.Date;
import lombok.Data;

/**
 * 外管礼包发放记录。
 *
 * @author luowenshen
 */
@Data
public class ExternalGiftPackIssue
{
    /** 发放记录主键 */
    private Long id;
    /** 礼包名称 */
    private String giftName;
    /** 接收用户UID */
    private Long receiverUid;
    /** 操作账号 */
    private String loginName;
    /** 发放内容快照 */
    private String contentSnapshot;
    /** 状态 */
    private String status;
    /** 发放时间 */
    private Date createTime;
}
