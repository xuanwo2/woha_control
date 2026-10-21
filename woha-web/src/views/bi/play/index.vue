<template>
  <div class="play-dashboard" v-loading="loading">
    <section class="dashboard-toolbar">
      <div class="dashboard-title">
        <div>
          <h1>玩法看板</h1>
          <p>P11-P16 · 投注类合规口径单独导出</p>
        </div>
        <span class="source-badge">业务结果表口径</span>
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
        <el-button v-hasPermi="['bi:play:export']" :icon="Download" @click="handleExport">合规导出</el-button>
      </div>
    </section>

    <section class="overview-panel">
      <header class="panel-header">
        <div>
          <h2>自研玩法总览 <span>game_type</span></h2>
          <p>{{ dashboard.startDate || '--' }} 至 {{ dashboard.endDate || '--' }}</p>
        </div>
        <span>更新于 {{ dashboard.updateTime || '--' }}</span>
      </header>
      <el-table :data="dashboard.games" class="overview-table">
        <el-table-column prop="gameName" label="玩法" min-width="210" />
        <el-table-column label="投注流水（P11）" min-width="155" align="right">
          <template #default="scope">{{ formatCompact(scope.row.betGold) }}</template>
        </el-table-column>
        <el-table-column label="返奖（P12·T+1）" min-width="155" align="right">
          <template #default="scope">{{ scope.row.reconciliationOnly ? '--' : formatCompact(scope.row.rewardGold) }}</template>
        </el-table-column>
        <el-table-column label="实际 RTP" min-width="125" align="right">
          <template #default="scope">{{ scope.row.reconciliationOnly ? '--' : formatRate(scope.row.rtp) }}</template>
        </el-table-column>
        <el-table-column label="毛利（P13）" min-width="145" align="right">
          <template #default="scope">{{ formatCompact(scope.row.marginGold) }}</template>
        </el-table-column>
        <el-table-column label="参与人数（P14）" min-width="145" align="right">
          <template #default="scope">{{ formatInteger(scope.row.participants) }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="175" align="center">
          <template #default="scope">
            <span class="status-tag" :class="`status-${scope.row.statusCode}`">{{ scope.row.statusName }}</span>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="lower-grid">
      <article class="segment-panel">
        <header class="panel-header">
          <div>
            <h2>砸蛋专区 · 付费/补偿分账</h2>
            <p>概率、公共奖池、个人奖池与定投记录</p>
          </div>
        </header>
        <el-table :data="dashboard.luckSegments" class="segment-table">
          <el-table-column prop="name" label="口径" min-width="185" />
          <el-table-column label="砸次/触发次数" min-width="130" align="right">
            <template #default="scope">{{ formatInteger(scope.row.plays) }}</template>
          </el-table-column>
          <el-table-column label="实际 RTP" min-width="115" align="right">
            <template #default="scope">{{ formatRate(scope.row.rtp) }}</template>
          </el-table-column>
          <el-table-column label="真实成本" min-width="135" align="right">
            <template #default="scope">{{ formatCompact(scope.row.costGold) }}</template>
          </el-table-column>
          <el-table-column label="标记" width="90" align="center">
            <template #default="scope">
              <span class="segment-tag" :class="scope.row.category === '补偿' ? 'tag-compensation' : 'tag-paid'">{{ scope.row.tag }}</span>
            </template>
          </el-table-column>
        </el-table>
        <footer class="panel-note">
          <span>概率与公共奖池归入付费口径，个人奖池和定投归入补偿口径。</span>
          <span>定投记录按已回填 input/output 的记录统计。</span>
        </footer>
      </article>

      <article class="funnel-panel">
        <header class="panel-header">
          <div>
            <h2>玩法转化漏斗 <span>P15 · 砸蛋</span></h2>
            <p>复投为同一自然日内投注两次及以上</p>
          </div>
        </header>
        <div class="funnel-list">
          <div v-for="(item, index) in dashboard.luckFunnel" :key="item.code" class="funnel-stage">
            <div
              class="funnel-bar"
              :class="{ 'is-pending': !item.available }"
              :style="{ width: funnelWidth(item, index) }"
            >
              <span>{{ item.name }}</span>
              <strong>{{ item.available ? formatInteger(item.value) : '未接入' }}</strong>
            </div>
          </div>
        </div>
        <footer class="funnel-note">
          曝光、进入阶段当前无砸蛋专属埋点数据源；投注和复投直接使用 play_luck_record。
        </footer>
      </article>
    </section>
  </div>
</template>

<script setup>
import { getCurrentInstance, onMounted, reactive, ref } from 'vue'
import { Download, Refresh } from '@element-plus/icons-vue'
import { getPlayDashboard, getPlayDimensions } from '@/api/bi/play'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const countries = ref([])
const query = reactive({ dateRange: defaultDateRange(), country: '' })
const dashboard = reactive({ startDate: '', endDate: '', updateTime: '', games: [], luckSegments: [], luckFunnel: [] })

onMounted(async () => {
  await loadDimensions()
  await loadDashboard()
})

async function loadDimensions() {
  const response = await getPlayDimensions()
  countries.value = response.data || []
}

async function loadDashboard() {
  if (!query.dateRange?.length) return
  loading.value = true
  try {
    const response = await getPlayDashboard(buildParams())
    Object.assign(dashboard, response.data || {})
  } finally {
    loading.value = false
  }
}

function handleExport() {
  proxy.download('bi/play/export', buildParams(), `玩法合规对账_${new Date().getTime()}.xlsx`)
}

function buildParams() {
  return { startDate: query.dateRange[0], endDate: query.dateRange[1], country: query.country }
}

function funnelWidth(item, index) {
  if (!item.available) return `${100 - index * 14}%`
  const available = dashboard.luckFunnel.filter(stage => stage.available)
  const base = Number(available[0]?.value || 0)
  if (!base) return '42%'
  return `${Math.max(24, Number(item.value || 0) * 58 / base + 22)}%`
}

function formatCompact(value) {
  const amount = Number(value || 0)
  if (Math.abs(amount) >= 10000) return `${(amount / 10000).toLocaleString('zh-CN', { maximumFractionDigits: 2 })}万`
  return amount.toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

function formatInteger(value) {
  return Number(value || 0).toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

function formatRate(value) {
  return `${Number(value || 0).toFixed(2)}%`
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
.play-dashboard { min-height: calc(100vh - 84px); padding: 20px; color: #202938; background: #f2f4f7; }
.dashboard-toolbar { min-height: 76px; padding: 14px 18px; display: flex; align-items: center; justify-content: space-between; gap: 18px; background: #fff; border: 1px solid #e5e9ef; border-radius: 6px; }
.dashboard-title { display: flex; align-items: center; gap: 20px; min-width: 0; }
.dashboard-title h1 { margin: 0; font-size: 21px; line-height: 1.2; letter-spacing: 0; }
.dashboard-title p { margin: 5px 0 0; color: #7f8a9b; font-size: 12px; }
.source-badge { padding: 7px 12px; color: #25835c; background: #eaf7f0; border-radius: 4px; font-size: 12px; white-space: nowrap; }
.filters { display: flex; align-items: center; gap: 8px; }
.filters :deep(.el-select) { width: 140px; }
.filters :deep(.el-date-editor) { width: 252px; }

.overview-panel, .segment-panel, .funnel-panel { margin-top: 14px; background: #fff; border: 1px solid #e5e9ef; border-radius: 6px; }
.panel-header { min-height: 74px; padding: 17px 20px; display: flex; align-items: flex-start; justify-content: space-between; gap: 14px; }
.panel-header h2 { margin: 0; font-size: 16px; line-height: 1.35; letter-spacing: 0; }
.panel-header h2 span { color: #7d8797; font-size: 12px; font-weight: 500; }
.panel-header p { margin: 5px 0 0; color: #8994a5; font-size: 12px; }
.panel-header > span { color: #8994a5; font-size: 12px; white-space: nowrap; }
.overview-table, .segment-table { width: 100%; }
.overview-table :deep(th.el-table__cell), .segment-table :deep(th.el-table__cell) { height: 52px; color: #657084; background: #f8f9fb; font-weight: 600; }
.overview-table :deep(td.el-table__cell) { height: 58px; }
.status-tag, .segment-tag { display: inline-block; min-width: 68px; padding: 5px 10px; border-radius: 3px; font-size: 12px; }
.status-normal { color: #23815a; background: #e8f7ef; }
.status-warning { color: #cb4056; background: #fff0f2; }
.status-reconciliation { color: #7052bd; background: #f1edff; }

.lower-grid { display: grid; grid-template-columns: minmax(0, 1.2fr) minmax(440px, .8fr); gap: 14px; }
.segment-panel, .funnel-panel { min-width: 0; }
.tag-paid { color: #7052bd; background: #f1edff; }
.tag-compensation { color: #23815a; background: #e8f7ef; }
.panel-note { min-height: 62px; padding: 12px 20px; display: flex; flex-direction: column; justify-content: center; gap: 4px; color: #8a95a6; border-top: 1px solid #edf0f4; font-size: 11px; line-height: 1.5; }

.funnel-list { min-height: 330px; padding: 10px 28px 20px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 14px; }
.funnel-stage { width: 100%; display: flex; justify-content: center; }
.funnel-bar { min-width: 180px; height: 58px; padding: 0 22px; display: flex; align-items: center; justify-content: center; gap: 10px; color: #fff; background: #8164cf; border-radius: 4px; transition: width .25s ease; }
.funnel-stage:nth-child(2) .funnel-bar { background: #9a84d8; }
.funnel-stage:nth-child(3) .funnel-bar { background: #b3a4e0; color: #302849; }
.funnel-stage:nth-child(4) .funnel-bar { background: #d1c9eb; color: #302849; }
.funnel-bar.is-pending { color: #7d6bb1; background: #f7f5fc; border: 1px dashed #b8aadf; }
.funnel-bar span { font-size: 13px; }
.funnel-bar strong { font-size: 14px; }
.funnel-note { padding: 12px 20px; color: #8a95a6; border-top: 1px solid #edf0f4; font-size: 11px; line-height: 1.6; }

@media (max-width: 1250px) {
  .dashboard-toolbar { align-items: flex-start; flex-direction: column; }
  .filters { width: 100%; flex-wrap: wrap; }
  .lower-grid { grid-template-columns: 1fr; }
}

@media (max-width: 680px) {
  .play-dashboard { padding: 12px; }
  .dashboard-title { align-items: flex-start; flex-direction: column; gap: 8px; }
  .filters :deep(.el-select), .filters :deep(.el-date-editor) { width: 100%; }
  .filters :deep(.el-button) { flex: 1; }
  .panel-header { align-items: flex-start; flex-direction: column; }
  .funnel-list { padding-right: 14px; padding-left: 14px; }
  .funnel-bar { min-width: 150px; }
}
</style>
