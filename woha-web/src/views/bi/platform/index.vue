<template>
  <div class="platform-dashboard" v-loading="loading">
    <section class="dashboard-toolbar">
      <div class="dashboard-heading">
        <h1>平台大盘</h1>
        <div class="status-line">
          <span class="freshness-badge">准实时 ≤5min</span>
          <span class="baseline-badge">基线：营收 3 日滚动挂 R22 · 毛利 7 日滚动挂 R23</span>
        </div>
      </div>

      <div class="filter-group">
        <el-select v-model="query.country" clearable placeholder="地区：全部" @change="loadDashboard">
          <el-option v-for="item in dimensions.countries" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.channel" clearable placeholder="渠道：全部" @change="loadDashboard">
          <el-option v-for="item in dimensions.channels" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.period" @change="loadDashboard">
          <el-option label="今日·实时" value="realtime" />
          <el-option label="日" value="day" />
          <el-option label="周" value="week" />
          <el-option label="月" value="month" />
        </el-select>
        <el-date-picker
          v-model="query.date"
          type="date"
          value-format="YYYY-MM-DD"
          :clearable="false"
          :disabled-date="disableFutureDate"
          @change="loadDashboard"
        />
        <el-tooltip content="刷新数据" placement="top">
          <el-button class="refresh-button" :icon="Refresh" circle @click="loadDashboard" />
        </el-tooltip>
      </div>
    </section>

    <section class="kpi-grid">
      <article
        v-for="(item, index) in dashboard.kpis"
        :key="item.code"
        class="kpi-card"
        :class="{ 'is-clickable': item.abnormal }"
        @click="openAbnormalDetail(item)"
      >
        <span v-if="item.abnormal" class="abnormal-dot" />
        <div class="metric-mark" :class="`tone-${index % 4}`">
          <span />
        </div>
        <div class="metric-content">
          <div class="metric-name">
            {{ item.name }} <span>({{ item.metric }} · {{ item.freshness }})</span>
          </div>
          <div class="metric-value">
            <template v-if="item.code === 'online'">
              {{ formatInteger(item.value) }}<small> 房间 / {{ item.unit }}</small>
            </template>
            <template v-else>
              <small v-if="item.unit === 'USD'">$</small>{{ formatMetric(item) }}<small v-if="item.unit !== 'USD'"> {{ item.unit }}</small>
            </template>
          </div>
        </div>
      </article>
    </section>

    <section class="analytics-grid">
      <article class="chart-panel trend-panel">
        <header class="panel-header">
          <div>
            <h2>充值 GMV / DAU 趋势</h2>
            <p>近 30 日 · 双轴</p>
          </div>
          <span>统计区间 {{ formatRange }}</span>
        </header>
        <div ref="trendChartRef" class="trend-chart" />
        <footer class="baseline-note">
          异常基线：营收按 3 日滚动均值，平台毛利按 7 日滚动均值；超过阈值卡片显示红点，点击可查看地区分解。
        </footer>
      </article>

      <aside class="side-column">
        <article class="chart-panel online-panel">
          <header class="panel-header compact">
            <div>
              <h2>在线监控</h2>
              <p>分钟级 · 最近 1 小时</p>
            </div>
            <span class="live-indicator"><i />实时</span>
          </header>
          <div ref="onlineChartRef" class="online-chart" />
        </article>

        <article class="chart-panel rank-panel">
          <header class="panel-header compact">
            <div>
              <h2>地区分布 Top</h2>
              <p>充值 GMV</p>
            </div>
          </header>
          <div v-if="dashboard.regionRanks.length" class="rank-list">
            <div v-for="(item, index) in dashboard.regionRanks" :key="item.country" class="rank-item">
              <div class="rank-meta">
                <span><b>{{ index + 1 }}</b>{{ item.country }}</span>
                <strong>${{ formatMoney(item.amount) }}</strong>
              </div>
              <div class="rank-track"><span :style="{ width: rankWidth(item.amount) }" /></div>
            </div>
          </div>
          <el-empty v-else description="暂无地区数据" :image-size="72" />
        </article>
      </aside>
    </section>

    <el-dialog v-model="detailVisible" :title="`${selectedMetric.name}异常维度分解`" width="620px">
      <el-alert
        :title="`当前指标较滚动基线变化 ${formatChangeRate(selectedMetric.changeRate)}`"
        type="warning"
        :closable="false"
        show-icon
      />
      <el-table :data="dashboard.regionRanks" class="detail-table">
        <el-table-column type="index" label="排名" width="72" />
        <el-table-column prop="country" label="地区" />
        <el-table-column prop="payUsers" label="付费人数" align="right" />
        <el-table-column label="充值 GMV" align="right">
          <template #default="scope">${{ formatMoney(scope.row.amount) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import { getPlatformDashboard, getPlatformDimensions } from '@/api/bi/platform'

const loading = ref(false)
const detailVisible = ref(false)
const selectedMetric = ref({})
const trendChartRef = ref()
const onlineChartRef = ref()
let trendChart
let onlineChart
let refreshTimer
let resizeObserver

const query = reactive({
  country: '',
  channel: '',
  period: 'realtime',
  date: formatDate(new Date())
})

const dimensions = reactive({ countries: [], channels: [] })
const dashboard = reactive({
  startTime: '',
  endTime: '',
  updateTime: '',
  kpis: [],
  trends: [],
  onlineTrends: [],
  regionRanks: []
})

const formatRange = computed(() => {
  if (!dashboard.startTime || !dashboard.endTime) return '--'
  return `${dashboard.startTime.slice(0, 10)} 至 ${dashboard.endTime.slice(0, 10)}`
})

onMounted(async () => {
  await loadDimensions()
  await loadDashboard()
  resizeObserver = new ResizeObserver(() => {
    trendChart?.resize()
    onlineChart?.resize()
  })
  resizeObserver.observe(document.querySelector('.platform-dashboard'))
  refreshTimer = window.setInterval(() => {
    if (query.period === 'realtime' && query.date === formatDate(new Date())) loadDashboard(false)
  }, 300000)
})

onBeforeUnmount(() => {
  window.clearInterval(refreshTimer)
  resizeObserver?.disconnect()
  trendChart?.dispose()
  onlineChart?.dispose()
})

async function loadDimensions() {
  const response = await getPlatformDimensions()
  Object.assign(dimensions, response.data || {})
}

async function loadDashboard(showLoading = true) {
  loading.value = showLoading
  try {
    const response = await getPlatformDashboard({ ...query })
    Object.assign(dashboard, response.data || {})
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  trendChart ||= echarts.init(trendChartRef.value)
  onlineChart ||= echarts.init(onlineChartRef.value)
  trendChart.setOption(buildTrendOption(), true)
  onlineChart.setOption(buildOnlineOption(), true)
}

function buildTrendOption() {
  return {
    animationDuration: 500,
    color: ['#4f86ed', '#8764d6'],
    tooltip: { trigger: 'axis', backgroundColor: '#1e293b', borderWidth: 0, textStyle: { color: '#fff' } },
    legend: { top: 0, right: 12, itemWidth: 10, itemHeight: 10, textStyle: { color: '#667085' } },
    grid: { top: 48, left: 64, right: 68, bottom: 40 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dashboard.trends.map(item => item.date.slice(5)),
      axisLine: { lineStyle: { color: '#d9dee8' } },
      axisTick: { show: false },
      axisLabel: { color: '#8a94a6', interval: 4 }
    },
    yAxis: [
      { type: 'value', name: 'USD', splitLine: { lineStyle: { color: '#edf0f5' } }, axisLabel: { color: '#8a94a6' } },
      { type: 'value', name: 'DAU', splitLine: { show: false }, axisLabel: { color: '#8a94a6' } }
    ],
    series: [
      { name: 'GMV', type: 'line', smooth: 0.25, symbol: 'none', lineStyle: { width: 3 }, data: dashboard.trends.map(item => item.gmv) },
      { name: 'DAU', type: 'line', yAxisIndex: 1, smooth: 0.25, symbol: 'none', lineStyle: { width: 3 }, data: dashboard.trends.map(item => item.dau) }
    ]
  }
}

function buildOnlineOption() {
  return {
    animationDuration: 500,
    color: ['#4f86ed'],
    tooltip: { trigger: 'axis', formatter: '{b}<br/>在线人数：{c}' },
    grid: { top: 18, left: 42, right: 14, bottom: 28 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dashboard.onlineTrends.map(item => item.time),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#d9dee8' } },
      axisLabel: { color: '#8a94a6', interval: 2 }
    },
    yAxis: { type: 'value', splitNumber: 3, axisLabel: { color: '#8a94a6' }, splitLine: { lineStyle: { color: '#edf0f5' } } },
    series: [{ type: 'line', smooth: 0.3, symbol: 'none', lineStyle: { width: 3 }, areaStyle: { color: 'rgba(79, 134, 237, 0.08)' }, data: dashboard.onlineTrends.map(item => item.onlineUsers) }]
  }
}

function formatMetric(item) {
  const value = Number(item.value || 0)
  if (item.unit === '%') return value.toFixed(2)
  if (item.unit === '金币' && Math.abs(value) >= 100000000) return `${(value / 100000000).toFixed(2)} 亿`
  if (item.unit === '金币' && Math.abs(value) >= 10000) return `${(value / 10000).toFixed(1)} 万`
  if (item.unit === 'USD') return formatMoney(value)
  return formatInteger(value)
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

function formatInteger(value) {
  return Number(value || 0).toLocaleString('en-US', { maximumFractionDigits: 0 })
}

function rankWidth(value) {
  const max = Number(dashboard.regionRanks[0]?.amount || 0)
  return max ? `${Math.max(8, Number(value) / max * 100)}%` : '0%'
}

function openAbnormalDetail(item) {
  if (!item.abnormal) return
  selectedMetric.value = item
  detailVisible.value = true
}

function formatChangeRate(value) {
  const rate = Number(value || 0) * 100
  return `${rate >= 0 ? '+' : ''}${rate.toFixed(2)}%`
}

function disableFutureDate(date) {
  return date.getTime() > Date.now()
}

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
</script>

<style scoped lang="scss">
.platform-dashboard {
  min-height: calc(100vh - 84px);
  padding: 20px;
  background: #f2f4f7;
  color: #202939;
}

.dashboard-toolbar {
  min-height: 74px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  background: #fff;
  border: 1px solid #e7eaf0;
  border-radius: 6px;
}

.dashboard-heading { display: flex; align-items: center; gap: 24px; min-width: 0; }
.dashboard-heading h1 { margin: 0; font-size: 20px; line-height: 1; letter-spacing: 0; white-space: nowrap; }
.status-line { display: flex; gap: 10px; min-width: 0; }
.freshness-badge, .baseline-badge { padding: 6px 16px; border-radius: 4px; font-size: 13px; white-space: nowrap; }
.freshness-badge { color: #249260; background: #e8f7ef; }
.baseline-badge { color: #a66b13; background: #fff6e5; }

.filter-group { display: flex; align-items: center; gap: 8px; }
.filter-group :deep(.el-select) { width: 138px; }
.filter-group :deep(.el-date-editor) { width: 148px; }
.refresh-button { flex: 0 0 32px; }

.kpi-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; margin-top: 16px; }
.kpi-card { position: relative; min-height: 132px; padding: 20px 22px; display: flex; align-items: center; gap: 18px; background: #fff; border: 1px solid #e7eaf0; border-radius: 6px; overflow: hidden; }
.kpi-card.is-clickable { cursor: pointer; }
.kpi-card.is-clickable:hover { border-color: #e16b45; box-shadow: 0 6px 20px rgba(49, 56, 77, 0.08); }
.abnormal-dot { position: absolute; top: 14px; right: 14px; width: 10px; height: 10px; border-radius: 50%; background: #e6532f; box-shadow: 0 0 0 4px #fff0eb; }
.metric-mark { flex: 0 0 66px; height: 66px; display: grid; place-items: center; border-radius: 50%; }
.metric-mark span { width: 34px; height: 34px; border-radius: 50%; }
.tone-0 { background: #e7f0ff; } .tone-0 span { background: #76a4ef; }
.tone-1 { background: #e5f7ee; } .tone-1 span { background: #67ca98; }
.tone-2 { background: #fff5dc; } .tone-2 span { background: #eac974; }
.tone-3 { background: #f3eafd; } .tone-3 span { background: #aa8ae1; }
.metric-content { min-width: 0; }
.metric-name { margin-bottom: 8px; color: #4c566a; font-size: 15px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.metric-name span { color: #8d96a8; font-size: 12px; }
.metric-value { font-size: 29px; line-height: 1.1; font-weight: 650; color: #171d2a; white-space: nowrap; }
.metric-value small { font-size: 14px; font-weight: 500; color: #667085; }

.analytics-grid { display: grid; grid-template-columns: minmax(0, 2fr) minmax(350px, 0.9fr); gap: 16px; margin-top: 16px; }
.chart-panel { background: #fff; border: 1px solid #e7eaf0; border-radius: 6px; }
.trend-panel { min-height: 550px; padding: 20px 22px; }
.side-column { display: grid; grid-template-rows: 270px minmax(280px, 1fr); gap: 16px; }
.online-panel, .rank-panel { padding: 18px 20px; }
.panel-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.panel-header h2 { margin: 0; font-size: 17px; line-height: 1.35; letter-spacing: 0; }
.panel-header p { margin: 3px 0 0; color: #8993a5; font-size: 12px; }
.panel-header > span { color: #8993a5; font-size: 12px; }
.panel-header.compact h2 { font-size: 15px; }
.live-indicator { display: inline-flex; align-items: center; gap: 6px; }
.live-indicator i { width: 7px; height: 7px; border-radius: 50%; background: #38a169; }
.trend-chart { height: 420px; margin-top: 12px; }
.online-chart { height: 185px; margin-top: 10px; }
.baseline-note { padding-top: 13px; border-top: 1px solid #eef0f4; color: #8993a5; font-size: 12px; line-height: 1.6; }
.rank-list { margin-top: 16px; }
.rank-item + .rank-item { margin-top: 14px; }
.rank-meta { display: flex; justify-content: space-between; gap: 12px; color: #4c566a; font-size: 13px; }
.rank-meta b { display: inline-grid; place-items: center; width: 20px; height: 20px; margin-right: 8px; border-radius: 4px; color: #4f86ed; background: #edf3ff; font-size: 11px; }
.rank-meta strong { color: #30394a; font-weight: 600; }
.rank-track { height: 7px; margin-top: 7px; border-radius: 4px; background: #edf0f5; overflow: hidden; }
.rank-track span { display: block; height: 100%; border-radius: 4px; background: #5387e8; }
.detail-table { margin-top: 16px; }

@media (max-width: 1380px) {
  .dashboard-toolbar { align-items: flex-start; }
  .dashboard-heading { align-items: flex-start; flex-direction: column; gap: 10px; }
  .status-line { flex-wrap: wrap; }
  .filter-group { flex-wrap: wrap; justify-content: flex-end; }
  .kpi-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 900px) {
  .platform-dashboard { padding: 12px; }
  .dashboard-toolbar, .dashboard-heading, .filter-group { width: 100%; }
  .dashboard-toolbar { flex-direction: column; }
  .filter-group { justify-content: flex-start; }
  .filter-group :deep(.el-select), .filter-group :deep(.el-date-editor) { width: calc(50% - 4px); }
  .kpi-grid, .analytics-grid { grid-template-columns: 1fr; }
  .kpi-card { min-height: 112px; }
  .side-column { grid-template-rows: 270px auto; }
}

@media (max-width: 520px) {
  .status-line { display: none; }
  .kpi-grid { grid-template-columns: 1fr; }
  .filter-group :deep(.el-select), .filter-group :deep(.el-date-editor) { width: 100%; }
  .metric-value { font-size: 25px; }
}
</style>
