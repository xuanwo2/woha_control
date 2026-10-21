<template>
  <div class="invite-dashboard" v-loading="loading">
    <section class="dashboard-toolbar">
      <div class="dashboard-heading">
        <div>
          <h1>邀请增长</h1>
          <p>用户增长 · 邀请 M2</p>
        </div>
        <span class="range-badge">近 7 日 · {{ dashboard.startDate || '--' }} 至 {{ dashboard.endDate || '--' }}</span>
      </div>
      <div class="filter-group">
        <el-select v-model="query.country" clearable placeholder="地区：全部" @change="handleFilterChange">
          <el-option v-for="item in dimensions.countries" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.channel" clearable placeholder="渠道：全部" @change="handleFilterChange">
          <el-option v-for="item in dimensions.channels" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.os" clearable placeholder="系统：全部" @change="handleFilterChange">
          <el-option v-for="item in dimensions.operatingSystems" :key="item" :label="item" :value="item" />
        </el-select>
        <el-date-picker
          v-model="query.date"
          type="date"
          value-format="YYYY-MM-DD"
          :clearable="false"
          :disabled-date="disableFutureDate"
          @change="handleFilterChange"
        />
        <el-tooltip content="刷新数据" placement="top">
          <el-button :icon="Refresh" circle @click="loadAll" />
        </el-tooltip>
      </div>
    </section>

    <section class="kpi-grid">
      <article v-for="item in kpiItems" :key="item.code" class="kpi-card">
        <div class="kpi-mark" :class="`tone-${item.tone}`"><span /></div>
        <div class="kpi-content">
          <span>{{ item.name }} <small>（{{ item.metric }}）</small></span>
          <strong :class="{ 'is-pending': item.pending }">{{ item.value }}</strong>
        </div>
      </article>
    </section>

    <section class="analytics-grid">
      <article class="chart-panel">
        <header class="panel-header">
          <div>
            <h2>邀请绑定趋势 <span>（U13 · 近 30 日）</span></h2>
            <p>按绑定关系创建日期统计新绑定用户</p>
          </div>
          <span>更新于 {{ dashboard.updateTime || '--' }}</span>
        </header>
        <div ref="trendChartRef" class="trend-chart" />
      </article>

      <article class="chart-panel funnel-panel">
        <header class="panel-header">
          <div>
            <h2>K 因子分解 <span>（邀请漏斗一期）</span></h2>
            <p>当前按有效邀请人数与绑定成功计算</p>
          </div>
        </header>
        <div class="funnel-list">
          <div v-for="item in dashboard.funnel" :key="item.name" class="funnel-row" :class="{ 'is-pending': !item.available }">
            <div>
              <strong>{{ item.name }}</strong>
              <span>（{{ item.metric }}）</span>
            </div>
            <b v-if="item.available">{{ formatInteger(item.value) }}</b>
            <em v-else>预留 · 待归因系统</em>
          </div>
        </div>
        <div class="factor-formula">
          <span>K 因子（U14）= 绑定新用户 ÷ 有效邀请人数</span>
          <strong>{{ Number(dashboard.kFactor || 0).toFixed(2) }}</strong>
        </div>
        <p class="funnel-note">一期仅统计“有效邀请人数 → 绑定成功”两阶段，点击与安装环节在归因系统接入后启用。</p>
      </article>
    </section>

    <section class="quality-panel">
      <header class="quality-header">
        <div>
          <h2>邀请质量分层 <span>（U16 · 按邀请人）</span></h2>
          <p>统计最近 30 日绑定用户的次留、付费、GMV 与设备聚集情况</p>
        </div>
        <el-button v-hasPermi="['bi:inviteGrowth:export']" :icon="Download" @click="handleExport">导出</el-button>
      </header>

      <el-table :data="qualityRows" class="quality-table" stripe>
        <el-table-column prop="inviter" label="邀请人" min-width="150" />
        <el-table-column prop="boundUsers" label="绑定新用户" min-width="120" align="right">
          <template #default="scope">{{ formatInteger(scope.row.boundUsers) }}</template>
        </el-table-column>
        <el-table-column label="次日留存" min-width="110" align="right">
          <template #default="scope">{{ formatRate(scope.row.d1Retention) }}</template>
        </el-table-column>
        <el-table-column label="付费率" min-width="100" align="right">
          <template #default="scope">{{ formatRate(scope.row.payRate) }}</template>
        </el-table-column>
        <el-table-column label="带来 GMV" min-width="130" align="right">
          <template #default="scope">${{ formatMoney(scope.row.gmv) }}</template>
        </el-table-column>
        <el-table-column prop="deviceStatus" label="设备聚集" min-width="130">
          <template #default="scope">
            <span :class="{ 'device-warning': Number(scope.row.maxDeviceCluster || 0) >= 5 }">{{ scope.row.deviceStatus }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="qualityName" label="质量评级" min-width="110" align="center">
          <template #default="scope">
            <span class="quality-tag" :class="`quality-${scope.row.qualityCode}`">{{ scope.row.qualityName }}</span>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="query.pageNum"
        v-model:limit="query.pageSize"
        @pagination="loadQuality"
      />
      <footer class="quality-note">同一邀请人下单设备账号达到 5 个及以上时标记为疑似作弊，供后续反作弊队列复核。</footer>
    </section>
  </div>
</template>

<script setup>
import { computed, getCurrentInstance, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { Download, Refresh } from '@element-plus/icons-vue'
import { getInviteGrowthDashboard, getInviteGrowthDimensions, getInviteQualityList } from '@/api/bi/inviteGrowth'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const trendChartRef = ref()
const qualityRows = ref([])
const total = ref(0)
let trendChart
let resizeObserver

const query = reactive({
  country: '',
  channel: '',
  os: '',
  date: formatDate(new Date()),
  pageNum: 1,
  pageSize: 10
})
const dimensions = reactive({ countries: [], channels: [], operatingSystems: [] })
const dashboard = reactive({
  startDate: '',
  endDate: '',
  updateTime: '',
  todayBindings: 0,
  activeInviters: 0,
  kFactor: 0,
  shareClickAvailable: false,
  shareClicks: null,
  invitedD1Retention: 0,
  bindingTrends: [],
  funnel: []
})

const kpiItems = computed(() => [
  { code: 'bindings', name: '邀请绑定', metric: 'U13 · 今日', value: formatInteger(dashboard.todayBindings), tone: 0 },
  { code: 'factor', name: 'K 因子', metric: 'U14 · 7 日', value: Number(dashboard.kFactor || 0).toFixed(2), tone: 1 },
  { code: 'clicks', name: '分享点击', metric: 'U15', value: dashboard.shareClickAvailable ? formatInteger(dashboard.shareClicks) : '未接入', tone: 2, pending: !dashboard.shareClickAvailable },
  { code: 'retention', name: '邀请新用户次留', metric: 'D1', value: formatRate(dashboard.invitedD1Retention), tone: 3 }
])

onMounted(async () => {
  await loadDimensions()
  await loadAll()
  resizeObserver = new ResizeObserver(() => trendChart?.resize())
  resizeObserver.observe(document.querySelector('.invite-dashboard'))
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  trendChart?.dispose()
})

async function loadDimensions() {
  const response = await getInviteGrowthDimensions()
  Object.assign(dimensions, response.data || {})
}

async function loadAll() {
  loading.value = true
  try {
    await Promise.all([loadDashboard(), loadQuality()])
  } finally {
    loading.value = false
  }
}

async function loadDashboard() {
  const response = await getInviteGrowthDashboard(buildParams())
  Object.assign(dashboard, response.data || {})
  await nextTick()
  renderTrendChart()
}

async function loadQuality() {
  const response = await getInviteQualityList({ ...buildParams(), pageNum: query.pageNum, pageSize: query.pageSize })
  qualityRows.value = response.rows || []
  total.value = response.total || 0
}

function handleFilterChange() {
  query.pageNum = 1
  loadAll()
}

function renderTrendChart() {
  trendChart ||= echarts.init(trendChartRef.value)
  trendChart.setOption({
    animationDuration: 450,
    color: ['#4f86ed'],
    tooltip: { trigger: 'axis', formatter: '{b}<br/>绑定用户：{c}' },
    grid: { top: 22, left: 52, right: 20, bottom: 34 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dashboard.bindingTrends.map(item => item.date.slice(5)),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#d9dee8' } },
      axisLabel: { color: '#7c879a', interval: 4 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#7c879a' },
      splitLine: { lineStyle: { color: '#edf0f5' } }
    },
    series: [{
      type: 'line',
      smooth: 0.18,
      showSymbol: false,
      lineStyle: { width: 3 },
      areaStyle: { color: 'rgba(79, 134, 237, 0.05)' },
      data: dashboard.bindingTrends.map(item => item.bindings)
    }]
  }, true)
}

function handleExport() {
  proxy.download('bi/invite-growth/quality/export', buildParams(), `邀请人质量分层_${new Date().getTime()}.xlsx`)
}

function buildParams() {
  return { country: query.country, channel: query.channel, os: query.os, date: query.date }
}

function formatRate(value) {
  return `${Number(value || 0).toFixed(1)}%`
}

function formatInteger(value) {
  return Number(value || 0).toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
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
.invite-dashboard { min-height: calc(100vh - 84px); padding: 20px; background: #f2f4f7; color: #202939; }
.dashboard-toolbar { min-height: 74px; padding: 14px 18px; display: flex; align-items: center; justify-content: space-between; gap: 20px; background: #fff; border: 1px solid #e6eaf0; border-radius: 6px; }
.dashboard-heading { display: flex; align-items: center; gap: 20px; min-width: 0; }
.dashboard-heading h1 { margin: 0; font-size: 20px; line-height: 1.2; letter-spacing: 0; }
.dashboard-heading p { margin: 4px 0 0; color: #8490a3; font-size: 12px; }
.range-badge { padding: 7px 12px; color: #316dcc; background: #edf4ff; border-radius: 4px; font-size: 12px; white-space: nowrap; }
.filter-group { display: flex; align-items: center; gap: 8px; }
.filter-group :deep(.el-select) { width: 132px; }
.filter-group :deep(.el-date-editor) { width: 148px; }

.kpi-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; margin-top: 16px; }
.kpi-card { min-height: 132px; padding: 20px 24px; display: flex; align-items: center; gap: 20px; background: #fff; border: 1px solid #e6eaf0; border-radius: 6px; }
.kpi-mark { flex: 0 0 70px; height: 70px; display: grid; place-items: center; border-radius: 50%; }
.kpi-mark span { width: 34px; height: 34px; border-radius: 50%; }
.tone-0 { background: #e3edff; } .tone-0 span { background: #7ba4ef; }
.tone-1 { background: #e3f6ed; } .tone-1 span { background: #73cc9b; }
.tone-2 { background: #f1eafb; } .tone-2 span { background: #a98cdd; }
.tone-3 { background: #fff3df; } .tone-3 span { background: #efbd70; }
.kpi-content { min-width: 0; }
.kpi-content > span { color: #596579; font-size: 14px; white-space: nowrap; }
.kpi-content small { color: #8b95a6; font-size: 12px; }
.kpi-content strong { display: block; margin-top: 8px; color: #202939; font-size: 30px; line-height: 1; }
.kpi-content strong.is-pending { color: #a56c16; font-size: 19px; }

.analytics-grid { display: grid; grid-template-columns: minmax(0, 1.05fr) minmax(520px, 0.95fr); gap: 16px; margin-top: 16px; }
.chart-panel { min-width: 0; min-height: 390px; padding: 20px 24px; background: #fff; border: 1px solid #e6eaf0; border-radius: 6px; }
.panel-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.panel-header h2, .quality-header h2 { margin: 0; font-size: 17px; line-height: 1.35; letter-spacing: 0; }
.panel-header h2 span, .quality-header h2 span { color: #667085; font-size: 14px; font-weight: 500; }
.panel-header p, .quality-header p { margin: 4px 0 0; color: #8a94a6; font-size: 12px; }
.panel-header > span { color: #8a94a6; font-size: 12px; white-space: nowrap; }
.trend-chart { height: 310px; margin-top: 12px; }
.funnel-list { margin-top: 22px; }
.funnel-row { min-height: 54px; padding: 0 18px; display: flex; align-items: center; justify-content: space-between; gap: 14px; border-bottom: 1px solid #edf0f4; }
.funnel-row:first-child { background: #f7f9fc; border-radius: 4px 4px 0 0; }
.funnel-row div { color: #344054; }
.funnel-row div span { color: #8a94a6; font-size: 12px; }
.funnel-row b { color: #4f86ed; font-size: 16px; }
.funnel-row em { padding: 5px 12px; color: #bc7d18; background: #fff6e6; border-radius: 3px; font-size: 12px; font-style: normal; }
.funnel-row.is-pending { color: #98a2b3; }
.factor-formula { min-height: 58px; padding: 0 18px; display: flex; align-items: center; justify-content: space-between; gap: 14px; color: #344054; font-weight: 600; }
.factor-formula strong { color: #4f86ed; font-size: 17px; }
.funnel-note { margin: 4px 0 0; padding-top: 12px; border-top: 1px solid #edf0f4; color: #8a94a6; font-size: 12px; line-height: 1.6; }

.quality-panel { margin-top: 16px; padding: 0 20px 14px; background: #fff; border: 1px solid #e6eaf0; border-radius: 6px; }
.quality-header { min-height: 76px; display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.quality-table { width: 100%; }
.quality-tag { display: inline-block; min-width: 58px; padding: 5px 9px; border-radius: 3px; font-size: 12px; }
.quality-premium { color: #24845a; background: #e8f7ef; }
.quality-good { color: #7254bf; background: #f1edff; }
.quality-normal { color: #64748b; background: #eef1f5; }
.quality-suspicious { color: #cc4055; background: #fff0f2; }
.device-warning { color: #c13f53; font-weight: 600; }
.quality-note { padding: 12px 0 2px; color: #8a94a6; font-size: 12px; }

@media (max-width: 1450px) {
  .dashboard-toolbar { align-items: flex-start; }
  .filter-group { flex-wrap: wrap; justify-content: flex-end; }
  .kpi-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .analytics-grid { grid-template-columns: 1fr; }
}

@media (max-width: 900px) {
  .invite-dashboard { padding: 12px; }
  .dashboard-toolbar, .dashboard-heading, .filter-group { width: 100%; }
  .dashboard-toolbar { flex-direction: column; }
  .dashboard-heading { justify-content: space-between; }
  .filter-group { justify-content: flex-start; }
  .filter-group :deep(.el-select), .filter-group :deep(.el-date-editor) { width: calc(50% - 4px); }
  .kpi-card { min-height: 112px; }
}

@media (max-width: 560px) {
  .dashboard-heading { align-items: flex-start; flex-direction: column; gap: 8px; }
  .filter-group :deep(.el-select), .filter-group :deep(.el-date-editor) { width: 100%; }
  .kpi-grid { grid-template-columns: 1fr; }
  .kpi-mark { flex-basis: 58px; height: 58px; }
  .kpi-mark span { width: 28px; height: 28px; }
  .chart-panel { padding: 18px 16px; }
  .quality-header { align-items: flex-start; padding: 16px 0; }
}
</style>
