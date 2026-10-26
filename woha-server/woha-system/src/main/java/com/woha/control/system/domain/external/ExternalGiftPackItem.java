package com.woha.control.system.domain.external;

import lombok.Data;

/**
 * 外管礼包内容。
 *
 * @author luowenshen
 */
@Data
public class ExternalGiftPackItem
{
    /** 内容主键 */
    private Long id;
    /** 礼包主键 */
    private Long giftId;
    /** 内容类型：PROP装扮，VIP会员卡 */
    private String itemType;
    /** 装扮道具主键 */
    private Long propId;
    /** 装扮道具类型 */
    private Integer propType;
    /** VIP等级 */
    private Integer vipLevel;
    /** 有效天数 */
    private Integer dayNum;
    /** 发放数量 */
    private Integer quantity;
    /** 展示顺序 */
    private Integer sortNo;
    /** 内容展示名称 */
    private String itemName;
}
