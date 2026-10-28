package com.woha.control.system.domain.external;

import java.util.Date;
import lombok.Data;
import com.woha.control.common.annotation.Excel;

/**
 * 外管公会列表数据
 *
 * @author luowenshen
 */
@Data
public class ExternalGuildVO
{
    /** 公会主键 */
    private Long familyId;
    /** 公会编号 */
    @Excel(name = "公会ID")
    private Long familyNo;
    /** 公会名称 */
    @Excel(name = "公会名称")
    private String familyName;
    /** 会长 Woha ID */
    @Excel(name = "会长ID")
    private Long captainChatNo;
    /** 会长昵称 */
    @Excel(name = "会长昵称")
    private String captainNickname;
    /** 负责人 Woha 用户 UID */
    private Long responsibleWohaUid;
    /** 所属 BD Leader 用户 UID */
    private Long bdLeaderWohaUid;
    /** 主播数量 */
    @Excel(name = "主播数")
    private Integer anchorCount;
    /** 范围内流水 */
    @Excel(name = "范围流水（金币）")
    private Long rangeFlow;
    /** 周流水 */
    @Excel(name = "周流水（金币）")
    private Long weekFlow;
    /** 月流水 */
    @Excel(name = "月流水（金币）")
    private Long monthFlow;
    /** 历史总流水 */
    @Excel(name = "总流水（金币）")
    private Long totalFlow;
    /** 创建来源 */
    @Excel(name = "创建方式")
    private String createMethod;
    /** 状态 */
    @Excel(name = "状态")
    private String statusName;
    /** 创建时间 */
    @Excel(name = "创建时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /** 负责人名称 */
    @Excel(name = "负责人")
    private String responsibleName;
    /** BD Leader 名称 */
    @Excel(name = "所属BD Leader")
    private String bdLeaderName;
}
