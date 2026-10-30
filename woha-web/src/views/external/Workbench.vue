<template>
  <div class="page">
    <h2>{{ t('dashboard') }}</h2>
    <section class="cards">
      <div v-for="item in cards" :key="item.label" class="card"><span>{{ item.label }}</span><strong>{{ number(item.value) }}</strong></div>
    </section>
    <el-card>
      <template #header>{{ t('agencyRevenueGold') }}</template>
      <el-descriptions :column="4">
        <el-descriptions-item :label="t('daily')">{{ number(data.dayFlow) }}</el-descriptions-item>
        <el-descriptions-item :label="t('weekly')">{{ number(data.weekFlow) }}</el-descriptions-item>
        <el-descriptions-item :label="t('monthly')">{{ number(data.monthFlow) }}</el-descriptions-item>
        <el-descriptions-item :label="t('total')">{{ number(data.totalFlow) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    <el-card class="space">
      <template #header>{{ t('quotaAndAuthorization') }}</template>
      <div v-if="quotaItems.length" class="quota-list">
        <div v-for="item in quotaItems" :key="item.label" class="quota-item"><span>{{ item.label }}</span><strong>{{ item.value }}</strong></div>
      </div>
      <span v-else class="muted">{{ t('noQuota') }}</span>
    </el-card>
    <el-card class="space">
      <template #header>{{ t('recentOperations') }}</template>
      <el-table v-if="data.recentOperations?.length" :data="data.recentOperations" size="small">
        <el-table-column :label="t('time')" width="170"><template #default="{ row }">{{ parseTime(row.createTime) }}</template></el-table-column>
        <el-table-column :label="t('operation')" width="140"><template #default="{ row }">{{ operationName(row.operationType) }}</template></el-table-column>
        <el-table-column prop="target" :label="t('target')" width="150" />
        <el-table-column prop="content" :label="t('content')" min-width="220" />
      </el-table>
      <span v-else class="muted">{{ t('noOperations') }}</span>
    </el-card>
  </div>
</template>

<script setup>
import { getExternalWorkbench } from '@/api/external/manager'
import { useExternalI18n } from '@/i18n/external'

const { proxy } = getCurrentInstance()
const { locale, t } = useExternalI18n()
const data = reactive({ giftPackQuotas: [], recentOperations: [] })
const number = value => Number(value || 0).toLocaleString(locale.value === 'zh-CN' ? 'zh-CN' : locale.value)
const cards = computed(() => [
  { label: 'BD Leader', value: data.bdLeaderCount }, { label: 'BD', value: data.bdCount },
  { label: t('agenciesCount'), value: data.guildCount }, { label: t('anchors'), value: data.anchorCount },
  { label: t('todayTeamRevenue'), value: data.dayFlow }
])
const quotaItems = computed(() => {
  const items = (data.giftPackQuotas || []).map(item => ({
    label: item.giftName,
    value: `${t('remaining')} ${Math.max(0, Number(item.quotaCount || 0) - Number(item.usedCount || 0))} / ${item.quotaCount || 0}`
  }))
  if (data.chatNoTotal !== undefined) items.push({ label: t('specialIds'), value: `${t('available')} ${data.chatNoAvailable || 0} / ${data.chatNoTotal || 0}` })
  if (data.todayBanCount !== undefined) items.push({ label: t('bans'), value: `${t('usedToday')} ${data.todayBanCount} / 10` })
  return items
})
const operationNames = {
  ISSUE_GIFT: '赠送礼包', ASSIGN_CHAT_NO: '分配靓号', BAN: '执行封禁', UNBAN: '解除封禁', CREATE_GUILD: '新建公会',
  CREATE_SUBORDINATE: '新增下级', CHANGE_PASSWORD: '修改密码', EXPORT_GUILD_FLOW: '导出公会流水', EXPORT_GUILD_LIST: '导出公会列表',
  EXPORT_TEAM_MEMBER: '导出团队成员', EXPORT_GIFT_ISSUE: '导出礼包记录', EXPORT_ACCOUNT_ASSIGNMENT: '导出账号分配记录', SUBMIT_REPORT: '提交举报'
}
const operationName = value => operationNames[value] || value
const parseTime = value => proxy.parseTime(value)
getExternalWorkbench().then(res => Object.assign(data, res.data))
</script>

<style scoped>
.page{padding:20px}.cards{display:grid;grid-template-columns:repeat(5,minmax(130px,1fr));gap:16px;margin:16px 0}.card,.quota-item{border:1px solid #dcdfe6;padding:18px;background:#fff}.card span,.quota-item span{display:block;color:#909399}.card strong,.quota-item strong{display:block;font-size:24px;margin-top:10px}.space{margin-top:16px}.quota-list{display:flex;flex-wrap:wrap;gap:12px}.quota-item{min-width:160px;flex:1}.quota-item strong{font-size:16px}.muted{color:#909399}@media(max-width:900px){.cards{grid-template-columns:repeat(2,1fr)}.quota-item{min-width:calc(50% - 6px)}}
</style>
