package com.woha.control.system.domain.external;

import java.util.Date;
import lombok.Data;
import com.woha.control.common.annotation.Excel;

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
    @Excel(name = "礼包名称")
    private String giftName;
    /** 接收用户UID */
    @Excel(name = "接收用户UID")
    private Long receiverUid;
    /** 操作账号 */
    @Excel(name = "操作账号")
    private String loginName;
    /** 发放内容快照 */
    @Excel(name = "发放内容")
    private String contentSnapshot;
    /** 状态 */
    @Excel(name = "状态")
    private String status;
    /** 发放时间 */
    @Excel(name = "发放时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
