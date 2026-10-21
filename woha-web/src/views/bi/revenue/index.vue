<template>
  <div class="revenue-dashboard" v-loading="loading">
    <section class="dashboard-toolbar">
      <div class="dashboard-title">
        <div>
          <h1>营收看板</h1>
          <p>M3 · 服务端口径 · 1 万金币/钻石 = 1 USD</p>
        </div>
        <span class="date-caption">{{ dashboard.startDate || '--' }} 至 {{ dashboard.endDate || '--' }}</span>
      </div>
      <div class="filters">
        <el-select v-model="query.country" clearable placeholder="地区：全部" @change="loadDashboard">
          <el-option v-for="item in countries" :key="item" :label="item" :value="item" />
        </el-select>
        <el-date-picker
          v-model="query.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          :clearable="false"
          :disabled-date="disableFutureDate"
          @change="loadDashboard"
        />
        <el-tooltip content="刷新数据" placement="top">
          <el-button :icon="Refresh" circle @click="loadDashboard" />
        </el-tooltip>
      </div>
    </section>

    <section class="primary-kpis">
      <article v-for="item in primaryKpis" :key="item.code" class="metric-card">
        <div class="metric-head">
          <span>{{ item.name }} <small>{{ item.metric }}</small></span>
          <component :is="item.icon" class="metric-icon" />
        </div>
        <strong>{{ item.value }}</strong>
        <p>{{ item.note }}</p>
      </article>
    </section>

    <section class="secondary-kpis">
      <article v-for="item in secondaryKpis" :key="item.code" class="compact-metric">
        <span>{{ item.name }} <small>{{ item.metric }}</small></span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.note }}</p>
      </article>
    </section>

    <section class="analysis-grid">
      <article class="panel trend-panel">
        <header class="panel-header">
          <div>
            <h2>营收与提现趋势</h2>
            <p>GMV、真营收与提现到账额</p>
          </div>
          <span>更新于 {{ dashboard.updateTime || '--' }}</span>
        </header>
        <div ref="trendChartRef" class="trend-chart" />
      </article>

      <article class="panel channel-panel">
        <header class="panel-header">
          <div>
            <h2>支付渠道占比 <span>R10</span></h2>
            <p>按成功充值金额统计</p>
          </div>
        </header>
        <div class="channel-content">
          <div ref="channelChartRef" class="channel-chart" />
          <div class="channel-list">
            <div v-for="(item, index) in dashboard.paymentChannels" :key="item.name" class="channel-row">
              <i :style="{ backgroundColor: chartColors[index % chartColors.length] }" />
              <span>{{ item.name }}</span>
              <b>{{ formatRate(item.ratio) }}</b>
              <em>${{ formatMoney(item.amount) }}</em>
            </div>
            <el-empty v-if="!dashboard.paymentChannels.length" description="暂无数据" :image-size="54" />
          </div>
        </div>
      </article>
    </section>

    <section class="detail-grid">
      <article class="panel tier-panel">
        <header class="panel-header">
          <div>
            <h2>充值档位分布 <span>R9</span></h2>
            <p>按订单美元金额聚合</p>
          </div>
        </header>
        <div ref="tierChartRef" class="tier-chart" />
      </article>

      <article class="panel business-panel">
        <header class="panel-header">
          <div>
            <h2>业务消费</h2>
            <p>礼物、会员与道具商城</p>
          </div>
        </header>
        <div class="business-list">
          <div class="business-row">
            <span>礼物流水 <small>R11</small></span>
            <strong>{{ formatCoin(dashboard.giftGold) }}</strong>
            <em>送礼 {{ formatInteger(dashboard.giftSendUsers) }} 人 · 收礼 {{ formatInteger(dashboard.giftReceiveUsers) }} 人</em>
          </div>
          <div class="business-row">
            <span>VIP 购买 <small>R14</small></span>
            <strong>${{ formatMoney(dashboard.vipAmount) }}</strong>
            <em>{{ formatInteger(dashboard.vipBuyUsers) }} 人 · {{ formatInteger(dashboard.vipBuyOrders) }} 单</em>
          </div>
          <div class="business-row">
            <span>贵族购买 <small>R15</small></span>
            <strong>{{ formatCoin(dashboard.nobleGold) }}</strong>
            <em>{{ formatInteger(dashboard.nobleBuyUsers) }} 人 · {{ formatInteger(dashboard.nobleBuyOrders) }} 单</em>
          </div>
          <div class="business-row">
            <span>道具商城 <small>R16</small></span>
            <strong>{{ formatCoin(dashboard.propGold) }}</strong>
            <em>{{ formatInteger(dashboard.propBuyUsers) }} 人</em>
          </div>
        </div>
      </article>

      <article class="panel wallet-panel">
        <header class="panel-header">
          <div>
            <h2>钱包沉淀 <span>R21</span></h2>
            <p>当前余额快照，不受日期范围影响</p>
          </div>
        </header>
        <div class="wallet-list">
          <div>
            <span>用户金币</span>
            <strong>{{ formatCoin(dashboard.walletGold) }}</strong>
            <em>≈ ${{ formatMoney(toUsd(dashboard.walletGold)) }}</em>
          </div>
          <div>
            <span>代理金币</span>
            <strong>{{ formatCoin(dashboard.walletProxyGold) }}</strong>
            <em>≈ ${{ formatMoney(toUsd(dashboard.walletProxyGold)) }}</em>
          </div>
          <div>
            <span>用户钻石</span>
            <strong>{{ formatInteger(dashboard.walletDiamond) }} 钻石</strong>
            <em>≈ ${{ formatMoney(toUsd(dashboard.walletDiamond)) }}</em>
          </div>
        </div>
      </article>
    </section>

    <footer class="definition-bar">
      <span>真营收 R22：成功充值到账金币，包含 PROXY_PAY 代理派发，排除红包下发。</span>
      <span>提现成功率 R19b：成功笔数 ÷ 已处理笔数，审核中不计入分母。</span>
    </footer>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { Coin, CreditCard, Money, Refresh, TrendCharts } from '@element-plus/icons-vue'
import { getRevenueDashboard, getRevenueDimensions } from '@/api/bi/revenue'

const loading = ref(false)
const countries = ref([])
const trendChartRef = ref()
const channelChartRef = ref()
const tierChartRef = ref()
let trendChart
let channelChart
let tierChart
let resizeObserver

// 营收结构图表色板，用于固定不同渠道的视觉区分
const chartColors = ['#3478d4', '#28a47a', '#e39b36', '#7b61c9', '#d95f74', '#4d9bb6', '#788697']
// 金币或钻石折算美元的固定汇率分母
const COIN_USD_RATIO = 10000
const query = reactive({ dateRange: defaultDateRange(), country: '' })
const dashboard = reactive({
  startDate: '', endDate: '', updateTime: '', gmv: 0, trueRevenue: 0, withdrawAmount: 0, netFlow: 0,
  activeUsers: 0, payUsers: 0, payRate: 0, arpu: 0, arppu: 0, firstPayUsers: 0,
  withdrawSuccessRate: 0, withdrawApplications: 0, processedWithdrawals: 0, successfulWithdrawals: 0,
  giftGold: 0, giftSendUsers: 0, giftReceiveUsers: 0, vipBuyUsers: 0, vipBuyOrders: 0, vipAmount: 0,
  nobleBuyUsers: 0, nobleBuyOrders: 0, nobleGold: 0, walletGold: 0, walletProxyGold: 0, walletDiamond: 0,
  propBuyUsers: 0, propGold: 0, paymentChannels: [], chargeTiers: [], trends: []
})

const primaryKpis = computed(() => [
  { code: 'gmv', name: '充值 GMV', metric: 'R1', value: `$${formatMoney(dashboard.gmv)}`, note: `付费 UV ${formatInteger(dashboard.payUsers)} 人`, icon: CreditCard },
  { code: 'revenue', name: '真营收', metric: 'R22 · newGold', value: `$${formatMoney(dashboard.trueRevenue)}`, note: '含币商/代理派发', icon: TrendCharts },
  { code: 'withdraw', name: '提现到账额', metric: 'R19a', value: `$${formatMoney(dashboard.withdrawAmount)}`, note: `成功 ${formatInteger(dashboard.successfulWithdrawals)} 笔`, icon: Money },
  { code: 'net', name: '净流水', metric: 'R20', value: `$${formatMoney(dashboard.netFlow)}`, note: '充值 GMV - 提现到账额', icon: Coin }
])

const secondaryKpis = computed(() => [
  { code: 'payRate', name: '付费率', metric: 'R4/R5', value: formatRate(dashboard.payRate), note: `${formatInteger(dashboard.payUsers)} / ${formatInteger(dashboard.activeUsers)} 人` },
  { code: 'arpu', name: 'ARPU', metric: 'R6', value: `$${formatMoney(dashboard.arpu)}`, note: '充值 GMV ÷ 活跃用户' },
  { code: 'arppu', name: 'ARPPU', metric: 'R7', value: `$${formatMoney(dashboard.arppu)}`, note: '充值 GMV ÷ 付费用户' },
  { code: 'firstPay', name: '首充人数', metric: 'R8', value: formatInteger(dashboard.firstPayUsers), note: '平台首次成功付费' },
  { code: 'withdrawRate', name: '提现成功率', metric: 'R19b', value: formatRate(dashboard.withdrawSuccessRate), note: `${formatInteger(dashboard.successfulWithdrawals)} / ${formatInteger(dashboard.processedWithdrawals)} 笔` }
])

onMounted(async () => {
  await loadDimensions()
  await loadDashboard()
  resizeObserver = new ResizeObserver(resizeCharts)
  resizeObserver.observe(document.querySelector('.revenue-dashboard'))
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  trendChart?.dispose()
  channelChart?.dispose()
  tierChart?.dispose()
})

async function loadDimensions() {
  const response = await getRevenueDimensions()
  countries.value = response.data || []
}

async function loadDashboard() {
  if (!query.dateRange?.length) return
  loading.value = true
  try {
    const response = await getRevenueDashboard({
      startDate: query.dateRange[0],
      endDate: query.dateRange[1],
      country: query.country
    })
    Object.assign(dashboard, response.data || {})
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  renderTrendChart()
  renderChannelChart()
  renderTierChart()
}

function renderTrendChart() {
  trendChart ||= echarts.init(trendChartRef.value)
  trendChart.setOption({
    color: ['#3478d4', '#28a47a', '#d95f74'],
    tooltip: { trigger: 'axis', valueFormatter: value => `$${formatMoney(value)}` },
    legend: { top: 0, right: 0, data: ['充值 GMV', '真营收', '提现到账'] },
    grid: { top: 48, left: 58, right: 22, bottom: 34 },
    xAxis: { type: 'category', boundaryGap: false, data: dashboard.trends.map(item => item.date.slice(5)), axisTick: { show: false }, axisLine: { lineStyle: { color: '#d9dee7' } } },
    yAxis: { type: 'value', axisLabel: { formatter: '${value}', color: '#7d8796' }, splitLine: { lineStyle: { color: '#edf0f4' } } },
    series: [
      { name: '充值 GMV', type: 'line', smooth: 0.18, symbol: 'none', lineStyle: { width: 3 }, data: dashboard.trends.map(item => item.gmv) },
      { name: '真营收', type: 'line', smooth: 0.18, symbol: 'none', lineStyle: { width: 2 }, data: dashboard.trends.map(item => item.trueRevenue) },
      { name: '提现到账', type: 'bar', barMaxWidth: 18, data: dashboard.trends.map(item => item.withdrawAmount) }
    ]
  }, true)
}

function renderChannelChart() {
  channelChart ||= echarts.init(channelChartRef.value)
  channelChart.setOption({
    color: chartColors,
    tooltip: { trigger: 'item', formatter: params => `${params.name}<br/>$${formatMoney(params.value)} · ${params.percent}%` },
    series: [{
      type: 'pie', radius: ['58%', '82%'], center: ['50%', '50%'], avoidLabelOverlap: true,
      label: { show: false }, emphasis: { scaleSize: 5 },
      data: dashboard.paymentChannels.map(item => ({ name: item.name, value: item.amount }))
    }]
  }, true)
}

function renderTierChart() {
  tierChart ||= echarts.init(tierChartRef.value)
  tierChart.setOption({
    color: ['#3478d4'],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: params => `${params[0].name}<br/>订单：${formatInteger(params[0].value)}` },
    grid: { top: 18, left: 52, right: 16, bottom: 46 },
    xAxis: { type: 'category', data: dashboard.chargeTiers.map(item => item.name), axisTick: { show: false }, axisLabel: { color: '#7d8796', interval: 0, rotate: dashboard.chargeTiers.length > 8 ? 35 : 0 } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#7d8796' }, splitLine: { lineStyle: { color: '#edf0f4' } } },
    series: [{ type: 'bar', barMaxWidth: 28, itemStyle: { borderRadius: [3, 3, 0, 0] }, data: dashboard.chargeTiers.map(item => item.orders) }]
  }, true)
}

function resizeCharts() {
  trendChart?.resize()
  channelChart?.resize()
  tierChart?.resize()
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatInteger(value) {
  return Number(value || 0).toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

function formatCoin(value) {
  return `${formatInteger(value)} 金币`
}

function formatRate(value) {
  return `${Number(value || 0).toFixed(2)}%`
}

function toUsd(value) {
  return Number(value || 0) / COIN_USD_RATIO
}

function disableFutureDate(date) {
  return date.getTime() > Date.now()
}

function defaultDateRange() {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 6)
  return [formatDate(start), formatDate(end)]
}

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
</script>

<style scoped lang="scss">
.revenue-dashboard { min-height: calc(100vh - 84px); padding: 20px; color: #202938; background: #f2f4f7; }
.dashboard-toolbar { min-height: 76px; padding: 14px 18px; display: flex; align-items: center; justify-content: space-between; gap: 18px; background: #fff; border: 1px solid #e5e9ef; border-radius: 6px; }
.dashboard-title { display: flex; align-items: center; gap: 20px; min-width: 0; }
.dashboard-title h1 { margin: 0; font-size: 21px; line-height: 1.2; letter-spacing: 0; }
.dashboard-title p { margin: 5px 0 0; color: #7f8a9b; font-size: 12px; }
.date-caption { padding: 7px 11px; color: #2868bd; background: #edf4fd; border-radius: 4px; font-size: 12px; white-space: nowrap; }
.filters { display: flex; align-items: center; gap: 8px; }
.filters :deep(.el-select) { width: 140px; }
.filters :deep(.el-date-editor) { width: 252px; }

.primary-kpis { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; margin-top: 14px; }
.metric-card { min-height: 144px; padding: 19px 20px; background: #fff; border: 1px solid #e5e9ef; border-top: 3px solid #3478d4; border-radius: 6px; }
.metric-card:nth-child(2) { border-top-color: #28a47a; }
.metric-card:nth-child(3) { border-top-color: #d95f74; }
.metric-card:nth-child(4) { border-top-color: #7b61c9; }
.metric-head { display: flex; align-items: center; justify-content: space-between; color: #526074; font-size: 14px; }
.metric-head small, .compact-metric small, .panel-header h2 span, .business-row small { color: #8a95a6; font-size: 11px; font-weight: 500; }
.metric-icon { width: 19px; color: #8692a3; }
.metric-card strong { display: block; margin-top: 17px; font-size: 28px; line-height: 1; }
.metric-card p, .compact-metric p { margin: 12px 0 0; color: #8994a5; font-size: 12px; }

.secondary-kpis { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); margin-top: 14px; background: #fff; border: 1px solid #e5e9ef; border-radius: 6px; }
.compact-metric { min-width: 0; min-height: 112px; padding: 18px 20px; border-right: 1px solid #edf0f4; }
.compact-metric:last-child { border-right: 0; }
.compact-metric > span { color: #596679; font-size: 13px; }
.compact-metric strong { display: block; margin-top: 10px; font-size: 23px; }
.compact-metric p { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.analysis-grid { display: grid; grid-template-columns: minmax(0, 1.35fr) minmax(430px, .65fr); gap: 14px; margin-top: 14px; }
.detail-grid { display: grid; grid-template-columns: minmax(0, 1.15fr) minmax(360px, .9fr) minmax(300px, .7fr); gap: 14px; margin-top: 14px; }
.panel { min-width: 0; padding: 19px 20px; background: #fff; border: 1px solid #e5e9ef; border-radius: 6px; }
.panel-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 14px; }
.panel-header h2 { margin: 0; font-size: 16px; line-height: 1.35; letter-spacing: 0; }
.panel-header p { margin: 4px 0 0; color: #8994a5; font-size: 12px; }
.panel-header > span { color: #8994a5; font-size: 12px; white-space: nowrap; }
.trend-chart { height: 320px; margin-top: 8px; }
.channel-content { min-height: 320px; display: grid; grid-template-columns: 170px minmax(0, 1fr); align-items: center; gap: 12px; }
.channel-chart { height: 210px; }
.channel-list { min-width: 0; max-height: 270px; overflow-y: auto; }
.channel-row { min-height: 38px; display: grid; grid-template-columns: 8px minmax(0, 1fr) 55px 88px; align-items: center; gap: 8px; border-bottom: 1px solid #f0f2f5; font-size: 12px; }
.channel-row i { width: 8px; height: 8px; border-radius: 50%; }
.channel-row span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.channel-row b { color: #344054; text-align: right; }
.channel-row em { color: #7d8796; font-style: normal; text-align: right; }
.tier-chart { height: 286px; margin-top: 8px; }

.business-list { margin-top: 13px; }
.business-row { min-height: 64px; display: grid; grid-template-columns: minmax(105px, 1fr) minmax(120px, auto); align-items: center; column-gap: 10px; border-bottom: 1px solid #edf0f4; }
.business-row:last-child { border-bottom: 0; }
.business-row > span { color: #536074; font-size: 13px; }
.business-row strong { font-size: 15px; text-align: right; }
.business-row em { grid-column: 1 / -1; margin-top: -13px; color: #8a95a6; font-size: 11px; font-style: normal; }
.wallet-list { margin-top: 14px; }
.wallet-list > div { padding: 13px 0; border-bottom: 1px solid #edf0f4; }
.wallet-list > div:last-child { border-bottom: 0; }
.wallet-list span { color: #647084; font-size: 12px; }
.wallet-list strong { display: block; margin-top: 6px; font-size: 18px; line-height: 1.2; }
.wallet-list em { display: block; margin-top: 5px; color: #28a47a; font-size: 11px; font-style: normal; }
.definition-bar { margin-top: 14px; padding: 12px 16px; display: flex; justify-content: space-between; gap: 20px; color: #687487; background: #fff; border: 1px solid #e5e9ef; border-radius: 6px; font-size: 12px; line-height: 1.6; }

@media (max-width: 1500px) {
  .primary-kpis { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .detail-grid { grid-template-columns: minmax(0, 1.2fr) minmax(340px, .8fr); }
  .wallet-panel { grid-column: 1 / -1; }
  .wallet-list { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
  .wallet-list > div { border-right: 1px solid #edf0f4; border-bottom: 0; }
  .wallet-list > div:last-child { border-right: 0; }
}

@media (max-width: 1050px) {
  .dashboard-toolbar { align-items: flex-start; flex-direction: column; }
  .filters { width: 100%; flex-wrap: wrap; }
  .secondary-kpis { grid-template-columns: repeat(2, 1fr); }
  .compact-metric { border-bottom: 1px solid #edf0f4; }
  .analysis-grid, .detail-grid { grid-template-columns: 1fr; }
  .wallet-panel { grid-column: auto; }
}

@media (max-width: 650px) {
  .revenue-dashboard { padding: 12px; }
  .dashboard-title { align-items: flex-start; flex-direction: column; gap: 8px; }
  .filters :deep(.el-select), .filters :deep(.el-date-editor) { width: 100%; }
  .primary-kpis, .secondary-kpis { grid-template-columns: 1fr; }
  .compact-metric { border-right: 0; }
  .channel-content { grid-template-columns: 1fr; }
  .channel-chart { height: 180px; }
  .wallet-list { grid-template-columns: 1fr; }
  .wallet-list > div { border-right: 0; border-bottom: 1px solid #edf0f4; }
  .definition-bar { flex-direction: column; gap: 4px; }
}
</style>
