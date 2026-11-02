<template>
  <div class="page">
    <el-form :model="query" inline>
      <el-form-item :label="t('statisticsDate')"><el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" :range-separator="t('rangeSeparator')" :start-placeholder="t('startDate')" :end-placeholder="t('endDate')" /></el-form-item>
      <el-form-item :label="t('responsible')"><el-select v-model="query.responsibleWohaUid" clearable :placeholder="t('select')"><el-option v-for="item in responsibles" :key="item.wohaUid" :label="label(item)" :value="item.wohaUid" /></el-select></el-form-item>
      <el-button type="primary" @click="search">{{ t('query') }}</el-button><el-button @click="reset">{{ t('reset') }}</el-button>
    </el-form>
    <section class="summary"><div><span>{{ t('rangeFlow') }}</span><strong>{{ number(summary.rangeFlow) }}</strong></div><div><span>{{ t('weeklyFlow') }}</span><strong>{{ number(summary.weekFlow) }}</strong></div><div><span>{{ t('monthlyFlow') }}</span><strong>{{ number(summary.monthFlow) }}</strong></div><div><span>{{ t('totalFlow') }}</span><strong>{{ number(summary.totalFlow) }}</strong></div></section>
    <div class="toolbar"><h2>{{ t('byGuildCoins') }}</h2><el-button v-if="canExport" @click="exportData">{{ t('export') }}</el-button></div>
    <el-table :data="rows" v-loading="loading" border show-summary :summary-method="getSummaries">
      <el-table-column prop="familyName" :label="t('agencies')" min-width="190"><template #default="{ row }">{{ row.familyName }}（{{ row.familyNo }}）</template></el-table-column><el-table-column prop="responsibleName" :label="t('responsible')" min-width="120" /><el-table-column prop="rangeFlow" :label="t('rangeFlow')" min-width="130"><template #default="{ row }">{{ number(row.rangeFlow) }}</template></el-table-column><el-table-column prop="weekFlow" :label="t('weeklyFlow')" min-width="130"><template #default="{ row }">{{ number(row.weekFlow) }}</template></el-table-column><el-table-column prop="monthFlow" :label="t('monthlyFlow')" min-width="130"><template #default="{ row }">{{ number(row.monthFlow) }}</template></el-table-column><el-table-column prop="totalFlow" :label="t('totalFlow')" min-width="130"><template #default="{ row }">{{ number(row.totalFlow) }}</template></el-table-column>
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />
  </div>
</template>

<script setup>
import { getExternalGuildFlowSummary, getExternalProfile, listExternalGuild, listExternalGuildResponsibles } from '@/api/external/manager'
import { useExternalI18n } from '@/i18n/external'
const { proxy } = getCurrentInstance()
const { locale, t } = useExternalI18n()
const today = new Date().toISOString().slice(0, 10)
const dateRange = ref([today, today]), responsibles = ref([]), rows = ref([]), total = ref(0), loading = ref(false), summary = reactive({}), profile = ref({})
const query = reactive({ pageNum: 1, pageSize: 10, responsibleWohaUid: undefined, startDate: today, endDate: today })
const number = value => Number(value || 0).toLocaleString(locale.value)
const canExport = computed(() => String(profile.value.permissions || '').split(',').includes('EXPORT_GUILD_FLOW'))
const label = item => `${item.realName || item.loginName}（${item.wohaChatNo || item.wohaUid}）`
function syncDate() { query.startDate = dateRange.value?.[0]; query.endDate = dateRange.value?.[1] }
async function load() { syncDate(); loading.value = true; try { const [list, totals] = await Promise.all([listExternalGuild(query), getExternalGuildFlowSummary(query)]); rows.value = list.rows || []; total.value = list.total || 0; Object.assign(summary, totals.data || {}) } finally { loading.value = false } }
async function loadResponsibles() { const res = await listExternalGuildResponsibles(); responsibles.value = res.data || [] }
function search() { query.pageNum = 1; load() }
function reset() { query.responsibleWohaUid = undefined; dateRange.value = [today, today]; search() }
function exportData() { syncDate(); proxy.download('external/guild/export', { responsibleWohaUid: query.responsibleWohaUid, startDate: query.startDate, endDate: query.endDate }, `${t('guildFlow')}_${Date.now()}.xlsx`) }
function getSummaries({ columns }) { const values = { rangeFlow: summary.rangeFlow, weekFlow: summary.weekFlow, monthFlow: summary.monthFlow, totalFlow: summary.totalFlow }; return columns.map((column, index) => index === 0 ? t('summaryTotal') : (values[column.property] === undefined ? '' : number(values[column.property]))) }
getExternalProfile().then(res => profile.value = res.data || {})
loadResponsibles(); load()
</script>

<style scoped>
.page{padding:20px}.summary{display:grid;grid-template-columns:repeat(4,minmax(160px,1fr));gap:16px;margin:0 0 16px}.summary div{border:1px solid #dcdfe6;background:#fff;padding:16px}.summary span{display:block;color:#909399}.summary strong{display:block;font-size:24px;margin-top:8px}.toolbar{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px}.toolbar h2{font-size:18px;margin:0}
.page :deep(.pagination-container) {
  display: flex;
  justify-content: flex-end;
  height: auto;
  margin: 12px 0 0;
  padding: 0 !important;
  background: transparent;
}
.page :deep(.pagination-container .el-pagination) {
  position: static;
}
@media(max-width:900px){.summary{grid-template-columns:repeat(2,1fr)}}
</style>
