<template>
  <div class="growth-dashboard" v-loading="loading">
    <section class="dashboard-toolbar">
      <div class="dashboard-heading">
        <div>
          <h1>用户增长</h1>
          <p>留存与漏斗 M2</p>
        </div>
        <span class="range-badge">近 7 日 · {{ dashboard.startDate || '--' }} 至 {{ dashboard.endDate || '--' }}</span>
      </div>

      <div class="filter-group">
        <el-select v-model="query.country" clearable placeholder="地区：全部" @change="loadDashboard">
          <el-option v-for="item in dimensions.countries" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.channel" clearable placeholder="渠道：全部" @change="loadDashboard">
          <el-option v-for="item in dimensions.channels" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.os" clearable placeholder="系统：全部" @change="loadDashboard">
          <el-option v-for="item in dimensions.operatingSystems" :key="item" :label="item" :value="item" />
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
          <el-button :icon="Refresh" circle @click="loadDashboard" />
        </el-tooltip>
      </div>
    </section>

    <section class="growth-layout">
      <article class="growth-panel overview-panel">
        <header class="panel-header">
          <div>
            <h2>注册转化漏斗 <span>（U8 · 近 7 日）</span></h2>
            <p>曝光、提交按设备或会话去重，注册成功和完善资料按 UID 去重</p>
          </div>
          <span class="data-time">更新于 {{ dashboard.updateTime || '--' }}</span>
        </header>

        <div class="funnel-list">
          <div v-for="(item, index) in dashboard.funnel" :key="item.code" class="funnel-row">
            <div class="funnel-bar-wrap">
              <div
                class="funnel-bar"
                :class="`funnel-tone-${index}`"
                :style="{ width: funnelWidth(item.value) }"
              >
                <span>{{ item.name }}<template v-if="item.code === 'profile'">（{{ item.metric }}）</template></span>
                <strong>{{ formatInteger(item.value) }}</strong>
              </div>
            </div>
            <b>{{ formatRate(item.rate) }}</b>
          </div>
        </div>

        <div class="login-summary">
          <div>
            <span>登录成功次数（U9）</span>
            <strong>{{ formatInteger(dashboard.loginSuccesses) }}</strong>
            <small>最近 7 日成功写入登录记录的次数</small>
          </div>
          <div>
            <span>第三方登录占比（U10）</span>
            <strong>{{ formatRate(dashboard.thirdLoginRate) }}</strong>
            <small>{{ thirdLoginSummary }}</small>
          </div>
        </div>

        <section class="retention-section">
          <header class="section-header">
            <div>
              <h2>留存曲线 <span>（U4/U5）</span></h2>
              <p>按注册同期群计算，D0 为注册当日活跃基准</p>
            </div>
          </header>
          <div ref="retentionChartRef" class="retention-chart" />
          <div class="retention-highlights">
            <div><span>次日</span><strong>{{ curveRate(1) }}</strong></div>
            <div><span>7 日</span><strong>{{ curveRate(7) }}</strong></div>
            <div><span>30 日</span><strong>{{ curveRate(30) }}</strong></div>
          </div>
        </section>
      </article>

      <article class="growth-panel matrix-panel">
        <header class="panel-header">
          <div>
            <h2>留存矩阵 <span>（U6 · 注册日 × 回访间隔 · 热力）</span></h2>
            <p>颜色越深代表留存率越高</p>
          </div>
        </header>

        <div class="matrix-scroll">
          <div class="retention-matrix">
            <div class="matrix-head">注册日</div>
            <div v-for="day in 7" :key="`head-${day}`" class="matrix-head">D{{ day - 1 }}</div>
            <template v-for="row in dashboard.retentionMatrix" :key="row.registerDate">
              <div class="matrix-date">
                <strong>{{ row.registerDate.slice(5) }}</strong>
                <span>{{ formatInteger(row.registeredUsers) }} 人</span>
              </div>
              <div
                v-for="(rate, dayIndex) in row.rates"
                :key="`${row.registerDate}-${dayIndex}`"
                class="matrix-cell"
                :class="{ 'is-empty': rate === null || rate === undefined }"
                :style="matrixCellStyle(rate)"
              >
                {{ rate === null || rate === undefined ? '' : formatRate(rate) }}
              </div>
            </template>
          </div>
        </div>

        <div class="matrix-note">
          <span>D0 为当日活跃基准 100%</span>
          <span>仅展示已到达观察日的数据</span>
        </div>

        <section class="channel-section">
          <header class="section-header">
            <div>
              <h2>第三方登录构成</h2>
              <p>最近 7 日登录成功记录</p>
            </div>
          </header>
          <div v-if="dashboard.thirdLoginChannels.length" class="channel-list">
            <div v-for="item in dashboard.thirdLoginChannels" :key="item.name" class="channel-item">
              <div class="channel-meta">
                <span>{{ item.name }}</span>
                <strong>{{ formatRate(item.rate) }}</strong>
              </div>
              <div class="channel-track"><span :style="{ width: `${Math.max(3, Number(item.rate || 0))}%` }" /></div>
              <small>{{ formatInteger(item.value) }} 次</small>
            </div>
          </div>
          <el-empty v-else description="暂无第三方登录数据" :image-size="72" />
        </section>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import { getUserGrowthDashboard, getUserGrowthDimensions } from '@/api/bi/userGrowth'

const loading = ref(false)
const retentionChartRef = ref()
let retentionChart
let resizeObserver

const query = reactive({
  country: '',
  channel: '',
  os: '',
  date: formatDate(new Date())
})

const dimensions = reactive({ countries: [], channels: [], operatingSystems: [] })
const dashboard = reactive({
  startDate: '',
  endDate: '',
  updateTime: '',
  funnel: [],
  retentionCurve: [],
  retentionMatrix: [],
  loginSuccesses: 0,
  thirdLoginRate: 0,
  thirdLoginChannels: []
})

const maxFunnelValue = computed(() => Math.max(0, ...dashboard.funnel.map(item => Number(item.value || 0))))
const thirdLoginSummary = computed(() => {
  if (!dashboard.thirdLoginChannels.length) return '暂无第三方登录记录'
  return dashboard.thirdLoginChannels.slice(0, 3).map(item => `${item.name} ${formatRate(item.rate)}`).join(' / ')
})

onMounted(async () => {
  await loadDimensions()
  await loadDashboard()
  resizeObserver = new ResizeObserver(() => retentionChart?.resize())
  resizeObserver.observe(document.querySelector('.growth-dashboard'))
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  retentionChart?.dispose()
})

async function loadDimensions() {
  const response = await getUserGrowthDimensions()
  Object.assign(dimensions, response.data || {})
}

async function loadDashboard() {
  loading.value = true
  try {
    const response = await getUserGrowthDashboard({ ...query })
    Object.assign(dashboard, response.data || {})
    await nextTick()
    renderRetentionChart()
  } finally {
    loading.value = false
  }
}

function renderRetentionChart() {
  retentionChart ||= echarts.init(retentionChartRef.value)
  retentionChart.setOption({
    animationDuration: 450,
    color: ['#4f86ed'],
    tooltip: {
      trigger: 'axis',
      formatter(params) {
        const item = params[0]
        return `${item.axisValue}<br/>留存率：${Number(item.value || 0).toFixed(2)}%`
      }
    },
    grid: { top: 22, left: 48, right: 20, bottom: 34 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dashboard.retentionCurve.map(item => `D${item.day}`),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#d9dee8' } },
      axisLabel: { color: '#7c879a', interval: 4 }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: { color: '#7c879a', formatter: '{value}%' },
      splitLine: { lineStyle: { color: '#edf0f5' } }
    },
    series: [{
      type: 'line',
      smooth: 0.25,
      showSymbol: false,
      lineStyle: { width: 3 },
      areaStyle: { color: 'rgba(79, 134, 237, 0.06)' },
      data: dashboard.retentionCurve.map(item => Number(item.rate || 0))
    }]
  }, true)
}

function funnelWidth(value) {
  const max = maxFunnelValue.value
  if (!max) return '58%'
  return `${Math.max(40, Number(value || 0) / max * 100)}%`
}

function matrixCellStyle(rate) {
  if (rate === null || rate === undefined) return {}
  const normalizedRate = Math.max(0, Math.min(100, Number(rate || 0)))
  const alpha = 0.14 + normalizedRate / 100 * 0.76
  return {
    backgroundColor: `rgba(64, 125, 232, ${alpha})`,
    color: normalizedRate >= 46 ? '#fff' : '#26344b'
  }
}

function curveRate(day) {
  const point = dashboard.retentionCurve.find(item => item.day === day)
  return formatRate(point?.rate)
}

function formatRate(value) {
  return `${Number(value || 0).toFixed(1)}%`
}

function formatInteger(value) {
  return Number(value || 0).toLocaleString('zh-CN', { maximumFractionDigits: 0 })
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
.growth-dashboard {
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
  border: 1px solid #e6eaf0;
  border-radius: 6px;
}

.dashboard-heading { display: flex; align-items: center; gap: 20px; min-width: 0; }
.dashboard-heading h1 { margin: 0; font-size: 20px; line-height: 1.2; letter-spacing: 0; }
.dashboard-heading p { margin: 4px 0 0; color: #8490a3; font-size: 12px; }
.range-badge { padding: 7px 12px; color: #316dcc; background: #edf4ff; border-radius: 4px; font-size: 12px; white-space: nowrap; }
.filter-group { display: flex; align-items: center; gap: 8px; }
.filter-group :deep(.el-select) { width: 132px; }
.filter-group :deep(.el-date-editor) { width: 148px; }

.growth-layout { display: grid; grid-template-columns: minmax(600px, 0.84fr) minmax(680px, 1.16fr); gap: 16px; margin-top: 16px; }
.growth-panel { min-width: 0; padding: 22px 24px; background: #fff; border: 1px solid #e6eaf0; border-radius: 6px; }
.panel-header, .section-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.panel-header h2, .section-header h2 { margin: 0; font-size: 17px; line-height: 1.35; letter-spacing: 0; }
.panel-header h2 span, .section-header h2 span { color: #667085; font-size: 14px; font-weight: 500; }
.panel-header p, .section-header p { margin: 4px 0 0; color: #8a94a6; font-size: 12px; }
.data-time { color: #8a94a6; font-size: 12px; white-space: nowrap; }
.funnel-list { padding: 18px 0 10px; }
.funnel-row { display: grid; grid-template-columns: minmax(0, 1fr) 58px; align-items: center; gap: 12px; }
.funnel-row + .funnel-row { margin-top: 10px; }
.funnel-bar-wrap { display: flex; justify-content: center; }
.funnel-bar { min-height: 58px; padding: 0 30px; display: flex; align-items: center; justify-content: center; gap: 10px; color: #19345f; clip-path: polygon(4% 0, 96% 0, 100% 100%, 0 100%); transition: width 0.25s ease; }
.funnel-bar span { font-size: 14px; white-space: nowrap; }
.funnel-bar strong { font-size: 16px; white-space: nowrap; }
.funnel-tone-0 { color: #fff; background: #5688e8; }
.funnel-tone-1 { background: #78a1ec; }
.funnel-tone-2 { background: #91b1eb; }
.funnel-tone-3 { background: #acc5ed; }
.funnel-row > b { color: #596579; font-size: 14px; text-align: right; }

.login-summary { display: grid; grid-template-columns: 1fr 1fr; gap: 0; margin-top: 8px; padding: 16px 0; border-top: 1px solid #edf0f4; border-bottom: 1px solid #edf0f4; }
.login-summary > div { padding: 0 18px; }
.login-summary > div + div { border-left: 1px solid #edf0f4; }
.login-summary span, .login-summary small { display: block; color: #7b8799; font-size: 12px; }
.login-summary strong { display: block; margin: 5px 0; color: #202939; font-size: 24px; }
.login-summary small { min-height: 18px; line-height: 1.5; }

.retention-section { padding-top: 20px; }
.retention-chart { height: 280px; margin-top: 8px; }
.retention-highlights { display: grid; grid-template-columns: repeat(3, 1fr); padding-top: 12px; border-top: 1px solid #edf0f4; }
.retention-highlights div { text-align: center; }
.retention-highlights div + div { border-left: 1px solid #edf0f4; }
.retention-highlights span { margin-right: 8px; color: #788397; font-size: 13px; }
.retention-highlights strong { color: #26344b; font-size: 17px; }

.matrix-scroll { margin-top: 22px; overflow-x: auto; }
.retention-matrix { min-width: 650px; display: grid; grid-template-columns: 96px repeat(7, minmax(68px, 1fr)); gap: 5px; }
.matrix-head { height: 28px; display: grid; place-items: center; color: #737f92; font-size: 12px; }
.matrix-date { height: 58px; padding-right: 8px; display: flex; flex-direction: column; align-items: flex-end; justify-content: center; }
.matrix-date strong { color: #4d596d; font-size: 13px; }
.matrix-date span { margin-top: 3px; color: #9aa3b2; font-size: 11px; }
.matrix-cell { height: 58px; display: grid; place-items: center; border-radius: 4px; font-size: 13px; font-weight: 600; }
.matrix-cell.is-empty { background: #f5f6f8; }
.matrix-note { margin-top: 14px; padding: 12px 0; display: flex; justify-content: space-between; gap: 12px; border-bottom: 1px solid #edf0f4; color: #8a94a6; font-size: 12px; }

.channel-section { padding-top: 22px; }
.channel-list { margin-top: 20px; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); column-gap: 24px; row-gap: 18px; }
.channel-item { position: relative; padding-bottom: 18px; }
.channel-meta { display: flex; justify-content: space-between; gap: 12px; color: #536074; font-size: 13px; }
.channel-meta strong { color: #2e3d55; }
.channel-track { height: 7px; margin-top: 8px; overflow: hidden; background: #edf0f5; border-radius: 4px; }
.channel-track span { display: block; height: 100%; background: #4f86ed; border-radius: 4px; }
.channel-item small { position: absolute; right: 0; bottom: 0; color: #9aa3b2; font-size: 11px; }

@media (max-width: 1500px) {
  .dashboard-toolbar { align-items: flex-start; }
  .filter-group { flex-wrap: wrap; justify-content: flex-end; }
  .growth-layout { grid-template-columns: 1fr; }
}

@media (max-width: 900px) {
  .growth-dashboard { padding: 12px; }
  .dashboard-toolbar, .dashboard-heading, .filter-group { width: 100%; }
  .dashboard-toolbar { flex-direction: column; }
  .dashboard-heading { justify-content: space-between; }
  .filter-group { justify-content: flex-start; }
  .filter-group :deep(.el-select), .filter-group :deep(.el-date-editor) { width: calc(50% - 4px); }
  .growth-panel { padding: 18px 16px; }
}

@media (max-width: 560px) {
  .dashboard-heading { align-items: flex-start; flex-direction: column; gap: 8px; }
  .filter-group :deep(.el-select), .filter-group :deep(.el-date-editor) { width: 100%; }
  .funnel-row { grid-template-columns: minmax(0, 1fr) 52px; }
  .funnel-bar { min-height: 52px; padding: 0 14px; gap: 6px; }
  .funnel-bar span { font-size: 12px; }
  .funnel-bar strong { font-size: 13px; }
  .login-summary, .channel-list { grid-template-columns: 1fr; }
  .login-summary > div + div { margin-top: 14px; padding-top: 14px; border-top: 1px solid #edf0f4; border-left: 0; }
  .matrix-note { flex-direction: column; }
}
</style>
