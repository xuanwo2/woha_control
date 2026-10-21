package com.woha.business.bi.domain;

import java.math.BigDecimal;
import com.woha.business.common.annotation.Excel;

/**
 * 玩法总览指标。
 *
 * @author luowenshen
 */
public class PlayGameOverviewVO
{
    /** 玩法业务编码 */
    private String gameCode;

    /** 玩法展示名称 */
    @Excel(name = "玩法")
    private String gameName;

    /** 投注金币流水 */
    @Excel(name = "投注流水（金币）")
    private BigDecimal betGold;

    /** 返奖金币流水 */
    @Excel(name = "返奖流水（金币）")
    private BigDecimal rewardGold;

    /** 实际 RTP 百分比 */
    @Excel(name = "实际 RTP（%）")
    private BigDecimal rtp;

    /** 净投注毛利 */
    @Excel(name = "净投注毛利（金币）")
    private BigDecimal marginGold;

    /** 参与用户数 */
    @Excel(name = "参与人数")
    private Long participants;

    /** 状态业务编码 */
    private String statusCode;

    /** 状态展示名称 */
    @Excel(name = "状态")
    private String statusName;

    /** 是否仅提供第三方对账口径 */
    private boolean reconciliationOnly;

    public String getGameCode() { return gameCode; }
    public void setGameCode(String gameCode) { this.gameCode = gameCode; }
    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }
    public BigDecimal getBetGold() { return betGold; }
    public void setBetGold(BigDecimal betGold) { this.betGold = betGold; }
    public BigDecimal getRewardGold() { return rewardGold; }
    public void setRewardGold(BigDecimal rewardGold) { this.rewardGold = rewardGold; }
    public BigDecimal getRtp() { return rtp; }
    public void setRtp(BigDecimal rtp) { this.rtp = rtp; }
    public BigDecimal getMarginGold() { return marginGold; }
    public void setMarginGold(BigDecimal marginGold) { this.marginGold = marginGold; }
    public Long getParticipants() { return participants; }
    public void setParticipants(Long participants) { this.participants = participants; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public boolean isReconciliationOnly() { return reconciliationOnly; }
    public void setReconciliationOnly(boolean reconciliationOnly) { this.reconciliationOnly = reconciliationOnly; }
}
