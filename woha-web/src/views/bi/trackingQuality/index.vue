<template>
  <div class="quality-page">
    <header class="quality-toolbar">
      <div class="heading-block">
        <h1>埋点质量监控</h1>
        <p>治理 / 埋点质量 M5 · 更新于 {{ dashboard.updateTime || '—' }}</p>
      </div>
      <el-button
        v-hasPermi="['bi:trackingQuality:refresh']"
        type="primary"
        :icon="Refresh"
        :loading="refreshing"
        @click="handleRefresh"
      >刷新检测</el-button>
    </header>

    <section class="kpi-grid" v-loading="dashboardLoading">
      <article v-for="item in kpis" :key="item.code" class="kpi-card">
        <div class="kpi-icon" :class="`tone-${item.tone}`">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="kpi-content">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}<small>{{ item.unit }}</small></strong>
          <p>{{ item.note }}</p>
        </div>
      </article>
    </section>

    <section class="data-band" v-loading="dashboardLoading">
      <div class="section-heading">
        <div>
          <h2>近 24 小时事件质量</h2>
          <p>与前 24 小时对比，聚合成功上报、校验拒绝与采集链路延迟</p>
        </div>
        <el-tag effect="plain">{{ dashboard.events.length }} 个启用事件</el-tag>
      </div>
      <el-table :data="dashboard.events" row-key="eventName" class="quality-table">
        <el-table-column prop="eventName" label="事件名" min-width="210">
          <template #default="scope"><code>{{ scope.row.eventName }}</code></template>
        </el-table-column>
        <el-table-column label="上报量" min-width="130" align="right">
          <template #default="scope">{{ formatCount(scope.row.reportCount) }}</template>
        </el-table-column>
        <el-table-column label="环比" width="110" align="right">
          <template #default="scope">
            <span :class="changeClass(scope.row.changeRate)">{{ signedPercent(scope.row.changeRate) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="漏报检测" width="118" align="center">
          <template #default="scope">
            <span class="state-label" :class="leakClass(scope.row.leakStatus)">{{ leakLabel(scope.row.leakStatus) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="参数错误率" width="130" align="right">
          <template #default="scope">
            <span :class="{ danger: Number(scope.row.errorRate) >= 5 }">{{ percent(scope.row.errorRate) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="P95 延迟" width="125" align="right">
          <template #default="scope">
            <span :class="{ danger: Number(scope.row.p95Latency) >= 3000 }">{{ formatLatency(scope.row.p95Latency) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="质量状态" width="120" align="center">
          <template #default="scope">
            <span class="health-badge" :class="`health-${scope.row.healthStatus.toLowerCase()}`">
              {{ healthLabel(scope.row.healthStatus) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="data-band release-band" v-loading="dashboardLoading">
      <div class="section-heading">
        <div>
          <h2>App 版本发布核对</h2>
          <p>对比版本发布区间内已审批字典变更与客户端实际事件</p>
        </div>
      </div>
      <el-table :data="dashboard.releases" row-key="versionNo" empty-text="暂无 App 发布版本">
        <el-table-column prop="versionNo" label="版本" width="110">
          <template #default="scope"><b>v{{ scope.row.versionNo }}</b></template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170" />
        <el-table-column label="应发布事件" min-width="220">
          <template #default="scope">{{ eventNames(scope.row.expectedEvents) }}</template>
        </el-table-column>
        <el-table-column label="实际检测事件" min-width="220">
          <template #default="scope">{{ eventNames(scope.row.actualEvents) }}</template>
        </el-table-column>
        <el-table-column label="核对结果" min-width="250">
          <template #default="scope">
            <span class="release-result" :class="`release-${scope.row.status.toLowerCase()}`">
              {{ releaseStatusLabel(scope.row.status) }}
            </span>
            <span class="release-conclusion">{{ scope.row.conclusion }}</span>
          </template>
        </el-table-column>
      </el-table>
    </section>

  </div>
</template>

<script setup name="TrackingQuality">
import { computed, onMounted, reactive, ref } from 'vue'
import { DataAnalysis, Refresh, UploadFilled, WarningFilled } from '@element-plus/icons-vue'
import auth from '@/plugins/auth'
import {
  getTrackingQualityDashboard,
  refreshTrackingQuality
} from '@/api/bi/trackingQuality'

const { proxy } = getCurrentInstance()

const dashboardLoading = ref(false)
const refreshing = ref(false)

const dashboard = reactive({
  dictionaryCoverage: 0,
  todayReports: 0,
  validationFailureRate: 0,
  updateTime: '',
  events: [],
  releases: []
})

const kpis = computed(() => [
  { code: 'coverage', label: '字典覆盖率', value: percent(dashboard.dictionaryCoverage), unit: '', note: '近 24 小时启用事件', icon: DataAnalysis, tone: 'blue' },
  { code: 'reports', label: '今日事件上报', value: formatCount(dashboard.todayReports), unit: '', note: '按服务端接收时间统计', icon: UploadFilled, tone: 'green' },
  { code: 'failure', label: '参数校验失败率', value: percent(dashboard.validationFailureRate), unit: '', note: '拒绝量 / 总请求量', icon: WarningFilled, tone: Number(dashboard.validationFailureRate) >= 5 ? 'red' : 'amber' }
])

onMounted(async () => {
  if (auth.hasPermi('bi:trackingQuality:refresh')) {
    await refreshTrackingQuality().catch(() => undefined)
  }
  await loadDashboard()
})

async function loadDashboard() {
  dashboardLoading.value = true
  try {
    const response = await getTrackingQualityDashboard()
    Object.assign(dashboard, response.data || {})
    dashboard.events = response.data?.events || []
    dashboard.releases = response.data?.releases || []
  } finally {
    dashboardLoading.value = false
  }
}

async function handleRefresh() {
  refreshing.value = true
  try {
    await refreshTrackingQuality()
    proxy.$modal.msgSuccess('质量检测已刷新')
    await loadDashboard()
  } finally {
    refreshing.value = false
  }
}

function formatCount(value) {
  const count = Number(value || 0)
  if (Math.abs(count) >= 100000000) return `${(count / 100000000).toFixed(2)} 亿`
  if (Math.abs(count) >= 10000) return `${(count / 10000).toFixed(1)} 万`
  return count.toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

function percent(value) {
  return `${Number(value || 0).toFixed(2)}%`
}

function signedPercent(value) {
  const number = Number(value || 0)
  return `${number > 0 ? '+' : ''}${number.toFixed(2)}%`
}

function formatLatency(value) {
  const latency = Number(value || 0)
  return latency >= 1000 ? `${(latency / 1000).toFixed(2)} 秒` : `${latency} 毫秒`
}

function changeClass(value) {
  const number = Number(value || 0)
  return number < -50 ? 'danger' : number > 0 ? 'positive' : ''
}

function leakLabel(value) {
  return { NORMAL: '正常', WARNING: '明显下降', MISSING: '疑似漏报' }[value] || '正常'
}

function leakClass(value) {
  return value === 'MISSING' ? 'state-danger' : value === 'WARNING' ? 'state-warning' : 'state-normal'
}

function healthLabel(value) {
  return { HEALTHY: '健康', RISK: '风险', WAITING: '待办跟进' }[value] || '未知'
}

function eventNames(values) {
  return values?.length ? values.join('、') : '—'
}

function releaseStatusLabel(value) {
  return { PASSED: '通过', WARNING: '缺失', EMPTY: '无变更' }[value] || '未知'
}

</script>

<style scoped lang="scss">
.quality-page { min-height: calc(100vh - 84px); padding: 20px; background: #f3f5f8; color: #202939; }
.quality-toolbar { min-height: 76px; padding: 15px 20px; display: flex; align-items: center; justify-content: space-between; gap: 20px; background: #fff; border: 1px solid #e5e9f0; border-radius: 6px; }
.heading-block { min-width: 0; }
.heading-block h1 { margin: 0; font-size: 21px; line-height: 1.25; letter-spacing: 0; }
.heading-block p { margin: 7px 0 0; color: #7d8799; font-size: 13px; }
.kpi-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; margin-top: 14px; }
.kpi-card { min-height: 128px; padding: 20px; display: flex; align-items: center; gap: 17px; background: #fff; border: 1px solid #e5e9f0; border-radius: 6px; }
.kpi-icon { flex: 0 0 54px; height: 54px; display: grid; place-items: center; border-radius: 6px; font-size: 25px; }
.tone-blue { color: #367be3; background: #eaf2ff; }
.tone-green { color: #218a5c; background: #e8f7ef; }
.tone-amber { color: #b06e0d; background: #fff4dc; }
.tone-red { color: #d94b3d; background: #fff0ed; }
.kpi-content { min-width: 0; }
.kpi-content > span { display: block; color: #657084; font-size: 13px; }
.kpi-content strong { display: block; margin-top: 6px; color: #151b27; font-size: 27px; line-height: 1.1; white-space: nowrap; }
.kpi-content strong small { margin-left: 5px; color: #6f7888; font-size: 13px; font-weight: 500; }
.kpi-content p { margin: 7px 0 0; color: #939baa; font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.data-band { margin-top: 14px; padding: 20px; background: #fff; border: 1px solid #e5e9f0; border-radius: 6px; }
.section-heading { margin-bottom: 16px; display: flex; align-items: flex-start; justify-content: space-between; gap: 18px; }
.section-heading h2 { margin: 0; font-size: 17px; line-height: 1.35; letter-spacing: 0; }
.section-heading p { margin: 5px 0 0; color: #8993a4; font-size: 12px; }
.quality-table code, .issue-band code { color: #345483; font-family: Consolas, monospace; }
.danger { color: #d44536; font-weight: 600; }
.positive { color: #218a5c; }
.muted { color: #a3aab6; }
.state-label, .health-badge, .release-result, .severity { display: inline-flex; align-items: center; justify-content: center; min-height: 24px; padding: 2px 8px; border-radius: 4px; font-size: 12px; white-space: nowrap; }
.state-normal, .health-healthy, .release-passed { color: #217a53; background: #e7f6ee; }
.state-warning, .health-risk, .release-empty, .severity-medium { color: #9a650e; background: #fff3dc; }
.state-danger, .health-waiting, .release-warning, .severity-high { color: #c33d32; background: #ffede9; }
.severity-low { color: #526174; background: #edf0f4; }
.release-result { margin-right: 9px; }
.release-conclusion { color: #606b7c; font-size: 13px; }
.issue-heading { align-items: center; }
.issue-filters { display: flex; align-items: center; gap: 8px; }
.issue-filters :deep(.el-select) { width: 132px; }
.issue-filters :deep(.el-input) { width: 190px; }
.issue-detail .issue-summary { display: flex; align-items: center; gap: 10px; }
.issue-detail .issue-summary strong { font-size: 16px; }
.issue-detail dl { margin: 18px 0; display: grid; grid-template-columns: 78px minmax(0, 1fr); gap: 11px 14px; padding: 15px; background: #f6f8fb; border-radius: 6px; font-size: 13px; }
.issue-detail dt { color: #7c8798; }
.issue-detail dd { margin: 0; color: #323b4b; line-height: 1.55; overflow-wrap: anywhere; }
.issue-detail :deep(.el-select) { width: 100%; }

@media (max-width: 1200px) {
  .kpi-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .issue-heading { align-items: flex-start; flex-direction: column; }
  .issue-filters { width: 100%; flex-wrap: wrap; }
}

@media (max-width: 720px) {
  .quality-page { padding: 10px; }
  .quality-toolbar { align-items: flex-start; flex-direction: column; }
  .kpi-grid { grid-template-columns: 1fr; }
  .data-band { padding: 14px; overflow: hidden; }
  .section-heading { flex-direction: column; }
  .issue-filters :deep(.el-select), .issue-filters :deep(.el-input) { width: 100%; }
}
</style>
