package com.woha.control.system.domain.external;

import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 外管礼包。
 *
 * @author luowenshen
 */
@Data
public class ExternalGiftPack
{
    /** 礼包主键 */
    private Long id;
    /** 礼包名称 */
    private String giftName;
    /** 赠送金币数量 */
    private Long goldAmount;
    /** 单用户可领取次数 */
    private Integer receiveLimit;
    /** 状态：0启用，1停用 */
    private String status;
    /** 创建者 */
    private String createBy;
    /** 更新者 */
    private String updateBy;
    /** 礼包内容 */
    private List<ExternalGiftPackItem> items;
    /** 创建时间 */
    private Date createTime;
}
