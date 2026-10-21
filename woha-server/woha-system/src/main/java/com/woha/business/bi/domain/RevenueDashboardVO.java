package com.woha.business.bi.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 营收看板聚合数据。
 *
 * @author luowenshen
 */
public class RevenueDashboardVO
{
    /** 统计开始日期 */
    private String startDate;
    /** 统计结束日期 */
    private String endDate;
    /** 数据更新时间 */
    private String updateTime;
    /** 充值总额，单位为美元 */
    private BigDecimal gmv;
    /** 真营收，按金币折算为美元 */
    private BigDecimal trueRevenue;
    /** 提现到账额，单位为美元 */
    private BigDecimal withdrawAmount;
    /** 净流水，单位为美元 */
    private BigDecimal netFlow;
    /** 活跃用户数 */
    private Long activeUsers;
    /** 付费用户数 */
    private Long payUsers;
    /** 付费率 */
    private BigDecimal payRate;
    /** 每活跃用户平均收入，单位为美元 */
    private BigDecimal arpu;
    /** 每付费用户平均收入，单位为美元 */
    private BigDecimal arppu;
    /** 首次付费用户数 */
    private Long firstPayUsers;
    /** 提现成功率 */
    private BigDecimal withdrawSuccessRate;
    /** 提现申请笔数 */
    private Long withdrawApplications;
    /** 已处理提现笔数 */
    private Long processedWithdrawals;
    /** 提现成功笔数 */
    private Long successfulWithdrawals;
    /** 礼物金币流水 */
    private BigDecimal giftGold;
    /** 送礼用户数 */
    private Long giftSendUsers;
    /** 收礼用户数 */
    private Long giftReceiveUsers;
    /** VIP 购买用户数 */
    private Long vipBuyUsers;
    /** VIP 购买订单数 */
    private Long vipBuyOrders;
    /** VIP 购买金额，单位为美元 */
    private BigDecimal vipAmount;
    /** 贵族购买用户数 */
    private Long nobleBuyUsers;
    /** 贵族购买订单数 */
    private Long nobleBuyOrders;
    /** 贵族购买金币 */
    private BigDecimal nobleGold;
    /** 用户钱包金币余额 */
    private BigDecimal walletGold;
    /** 币商钱包代理金币余额 */
    private BigDecimal walletProxyGold;
    /** 用户钱包钻石余额 */
    private BigDecimal walletDiamond;
    /** 道具商城消费用户数 */
    private Long propBuyUsers;
    /** 道具商城消费金币 */
    private BigDecimal propGold;
    /** 支付渠道占比 */
    private List<BreakdownItem> paymentChannels = new ArrayList<>();
    /** 充值档位分布 */
    private List<BreakdownItem> chargeTiers = new ArrayList<>();
    /** 营收日趋势 */
    private List<TrendPoint> trends = new ArrayList<>();

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public BigDecimal getGmv() { return gmv; }
    public void setGmv(BigDecimal gmv) { this.gmv = gmv; }
    public BigDecimal getTrueRevenue() { return trueRevenue; }
    public void setTrueRevenue(BigDecimal trueRevenue) { this.trueRevenue = trueRevenue; }
    public BigDecimal getWithdrawAmount() { return withdrawAmount; }
    public void setWithdrawAmount(BigDecimal withdrawAmount) { this.withdrawAmount = withdrawAmount; }
    public BigDecimal getNetFlow() { return netFlow; }
    public void setNetFlow(BigDecimal netFlow) { this.netFlow = netFlow; }
    public Long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }
    public Long getPayUsers() { return payUsers; }
    public void setPayUsers(Long payUsers) { this.payUsers = payUsers; }
    public BigDecimal getPayRate() { return payRate; }
    public void setPayRate(BigDecimal payRate) { this.payRate = payRate; }
    public BigDecimal getArpu() { return arpu; }
    public void setArpu(BigDecimal arpu) { this.arpu = arpu; }
    public BigDecimal getArppu() { return arppu; }
    public void setArppu(BigDecimal arppu) { this.arppu = arppu; }
    public Long getFirstPayUsers() { return firstPayUsers; }
    public void setFirstPayUsers(Long firstPayUsers) { this.firstPayUsers = firstPayUsers; }
    public BigDecimal getWithdrawSuccessRate() { return withdrawSuccessRate; }
    public void setWithdrawSuccessRate(BigDecimal withdrawSuccessRate) { this.withdrawSuccessRate = withdrawSuccessRate; }
    public Long getWithdrawApplications() { return withdrawApplications; }
    public void setWithdrawApplications(Long withdrawApplications) { this.withdrawApplications = withdrawApplications; }
    public Long getProcessedWithdrawals() { return processedWithdrawals; }
    public void setProcessedWithdrawals(Long processedWithdrawals) { this.processedWithdrawals = processedWithdrawals; }
    public Long getSuccessfulWithdrawals() { return successfulWithdrawals; }
    public void setSuccessfulWithdrawals(Long successfulWithdrawals) { this.successfulWithdrawals = successfulWithdrawals; }
    public BigDecimal getGiftGold() { return giftGold; }
    public void setGiftGold(BigDecimal giftGold) { this.giftGold = giftGold; }
    public Long getGiftSendUsers() { return giftSendUsers; }
    public void setGiftSendUsers(Long giftSendUsers) { this.giftSendUsers = giftSendUsers; }
    public Long getGiftReceiveUsers() { return giftReceiveUsers; }
    public void setGiftReceiveUsers(Long giftReceiveUsers) { this.giftReceiveUsers = giftReceiveUsers; }
    public Long getVipBuyUsers() { return vipBuyUsers; }
    public void setVipBuyUsers(Long vipBuyUsers) { this.vipBuyUsers = vipBuyUsers; }
    public Long getVipBuyOrders() { return vipBuyOrders; }
    public void setVipBuyOrders(Long vipBuyOrders) { this.vipBuyOrders = vipBuyOrders; }
    public BigDecimal getVipAmount() { return vipAmount; }
    public void setVipAmount(BigDecimal vipAmount) { this.vipAmount = vipAmount; }
    public Long getNobleBuyUsers() { return nobleBuyUsers; }
    public void setNobleBuyUsers(Long nobleBuyUsers) { this.nobleBuyUsers = nobleBuyUsers; }
    public Long getNobleBuyOrders() { return nobleBuyOrders; }
    public void setNobleBuyOrders(Long nobleBuyOrders) { this.nobleBuyOrders = nobleBuyOrders; }
    public BigDecimal getNobleGold() { return nobleGold; }
    public void setNobleGold(BigDecimal nobleGold) { this.nobleGold = nobleGold; }
    public BigDecimal getWalletGold() { return walletGold; }
    public void setWalletGold(BigDecimal walletGold) { this.walletGold = walletGold; }
    public BigDecimal getWalletProxyGold() { return walletProxyGold; }
    public void setWalletProxyGold(BigDecimal walletProxyGold) { this.walletProxyGold = walletProxyGold; }
    public BigDecimal getWalletDiamond() { return walletDiamond; }
    public void setWalletDiamond(BigDecimal walletDiamond) { this.walletDiamond = walletDiamond; }
    public Long getPropBuyUsers() { return propBuyUsers; }
    public void setPropBuyUsers(Long propBuyUsers) { this.propBuyUsers = propBuyUsers; }
    public BigDecimal getPropGold() { return propGold; }
    public void setPropGold(BigDecimal propGold) { this.propGold = propGold; }
    public List<BreakdownItem> getPaymentChannels() { return paymentChannels; }
    public void setPaymentChannels(List<BreakdownItem> paymentChannels) { this.paymentChannels = paymentChannels; }
    public List<BreakdownItem> getChargeTiers() { return chargeTiers; }
    public void setChargeTiers(List<BreakdownItem> chargeTiers) { this.chargeTiers = chargeTiers; }
    public List<TrendPoint> getTrends() { return trends; }
    public void setTrends(List<TrendPoint> trends) { this.trends = trends; }

    /**
     * 营收结构分布项。
     *
     * @author luowenshen
     */
    public static class BreakdownItem
    {
        /** 分布项名称 */
        private String name;
        /** 金额，单位为美元 */
        private BigDecimal amount;
        /** 订单数量 */
        private Long orders;
        /** 用户数量 */
        private Long users;
        /** 金额占比 */
        private BigDecimal ratio;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Long getOrders() { return orders; }
        public void setOrders(Long orders) { this.orders = orders; }
        public Long getUsers() { return users; }
        public void setUsers(Long users) { this.users = users; }
        public BigDecimal getRatio() { return ratio; }
        public void setRatio(BigDecimal ratio) { this.ratio = ratio; }
    }

    /**
     * 营收趋势数据点。
     *
     * @author luowenshen
     */
    public static class TrendPoint
    {
        /** 统计日期 */
        private String date;
        /** 当日充值总额，单位为美元 */
        private BigDecimal gmv;
        /** 当日真营收，单位为美元 */
        private BigDecimal trueRevenue;
        /** 当日提现到账额，单位为美元 */
        private BigDecimal withdrawAmount;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public BigDecimal getGmv() { return gmv; }
        public void setGmv(BigDecimal gmv) { this.gmv = gmv; }
        public BigDecimal getTrueRevenue() { return trueRevenue; }
        public void setTrueRevenue(BigDecimal trueRevenue) { this.trueRevenue = trueRevenue; }
        public BigDecimal getWithdrawAmount() { return withdrawAmount; }
        public void setWithdrawAmount(BigDecimal withdrawAmount) { this.withdrawAmount = withdrawAmount; }
    }
}
