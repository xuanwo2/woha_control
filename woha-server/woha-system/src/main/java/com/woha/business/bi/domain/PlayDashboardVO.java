package com.woha.business.bi.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 玩法看板聚合数据。
 *
 * @author luowenshen
 */
public class PlayDashboardVO
{
    /** 统计开始日期 */
    private String startDate;
    /** 统计结束日期 */
    private String endDate;
    /** 数据更新时间 */
    private String updateTime;
    /** 自研玩法总览 */
    private List<PlayGameOverviewVO> games = new ArrayList<>();
    /** 砸蛋口径拆分 */
    private List<LuckSegment> luckSegments = new ArrayList<>();
    /** 砸蛋转化漏斗 */
    private List<FunnelStage> luckFunnel = new ArrayList<>();

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public List<PlayGameOverviewVO> getGames() { return games; }
    public void setGames(List<PlayGameOverviewVO> games) { this.games = games; }
    public List<LuckSegment> getLuckSegments() { return luckSegments; }
    public void setLuckSegments(List<LuckSegment> luckSegments) { this.luckSegments = luckSegments; }
    public List<FunnelStage> getLuckFunnel() { return luckFunnel; }
    public void setLuckFunnel(List<FunnelStage> luckFunnel) { this.luckFunnel = luckFunnel; }

    /**
     * 砸蛋口径拆分项。
     *
     * @author luowenshen
     */
    public static class LuckSegment
    {
        /** 口径业务编码 */
        private String code;
        /** 口径展示名称 */
        private String name;
        /** 口径分类 */
        private String category;
        /** 砸蛋次数或补偿触发次数 */
        private Long plays;
        /** 实际 RTP 百分比 */
        private BigDecimal rtp;
        /** 真实产出成本，单位为金币 */
        private BigDecimal costGold;
        /** 口径标签 */
        private String tag;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Long getPlays() { return plays; }
        public void setPlays(Long plays) { this.plays = plays; }
        public BigDecimal getRtp() { return rtp; }
        public void setRtp(BigDecimal rtp) { this.rtp = rtp; }
        public BigDecimal getCostGold() { return costGold; }
        public void setCostGold(BigDecimal costGold) { this.costGold = costGold; }
        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }
    }

    /**
     * 砸蛋漏斗阶段。
     *
     * @author luowenshen
     */
    public static class FunnelStage
    {
        /** 阶段业务编码 */
        private String code;
        /** 阶段展示名称 */
        private String name;
        /** 阶段用户数，数据源未接入时为空 */
        private Long value;
        /** 阶段是否已有真实数据源 */
        private boolean available;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getValue() { return value; }
        public void setValue(Long value) { this.value = value; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }
}
