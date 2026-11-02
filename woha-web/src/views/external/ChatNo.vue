<template>
  <div class="page">
    <div class="metric-grid">
      <section class="metric"><span>{{ t('poolAvailable') }}</span><strong>{{ availableCount }} / {{ items.length }}</strong><small>{{ t('poolAllocatedByOperations') }}</small></section>
      <section class="metric"><span>{{ t('allocated') }}</span><strong>{{ assignedCount }}</strong></section>
    </div>
    <section class="pool-panel">
      <h2>{{ t('mySpecialIdPool') }}</h2>
      <el-table :data="items" v-loading="loading" border size="small" :empty-text="t('noSpecialIds')">
        <el-table-column prop="chatNo" :label="t('specialId')" min-width="110" />
        <el-table-column :label="t('status')" min-width="100"><template #default="{ row }"><el-tag :type="row.status === '0' ? 'success' : 'warning'">{{ row.status === '0' ? t('available') : t('allocated') }}</el-tag></template></el-table-column>
        <el-table-column prop="previousChatNo" :label="t('originalReceiverId')" min-width="150"><template #default="{ row }">{{ row.previousChatNo ?? '--' }}</template></el-table-column>
        <el-table-column prop="expireTime" :label="t('expiresAt')" min-width="160"><template #default="{ row }">{{ row.expireTime || '--' }}</template></el-table-column>
      </el-table>
    </section>
    <section v-if="canAssign" class="assign-panel">
      <h2>{{ t('assignSpecialId') }}</h2>
      <el-form :model="assignForm" label-width="86px">
        <el-form-item :label="t('specialId')"><el-select v-model="assignForm.chatNo" filterable :placeholder="t('selectAvailableSpecialId')"><el-option v-for="item in availableItems" :key="item.chatNo" :label="item.chatNo" :value="item.chatNo" /></el-select></el-form-item>
        <el-form-item :label="t('linkedWohaId')"><el-input v-model.number="assignForm.receiverChatNo" :placeholder="t('enter')" /></el-form-item>
        <el-form-item :label="t('validity')"><el-select v-model="assignForm.dayNum"><el-option :label="t('day30')" :value="30" /><el-option :label="t('day90')" :value="90" /><el-option :label="t('day180')" :value="180" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="assign">{{ t('assign') }}</el-button></el-form-item>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { assignExternalChatNo, getExternalProfile, listExternalChatNos } from '@/api/external/manager'
import { useExternalI18n } from '@/i18n/external'

const loading = ref(false)
const { t } = useExternalI18n()
const items = ref([])
const profile = ref({})
const assignForm = reactive({ chatNo: undefined, receiverChatNo: undefined, dayNum: 30 })
const availableItems = computed(() => items.value.filter(item => item.status === '0'))
const availableCount = computed(() => availableItems.value.length)
const assignedCount = computed(() => items.value.filter(item => item.status === '1').length)
const canAssign = computed(() => String(profile.value.permissions || '').split(',').includes('ASSIGN_CHAT_NO'))

async function load() {
  loading.value = true
  try {
    const response = await listExternalChatNos()
    items.value = response.data || []
  } finally {
    loading.value = false
  }
}

async function assign() {
  if (!assignForm.chatNo || !assignForm.receiverChatNo) return ElMessage.warning(t('selectSpecialIdAndUser'))
  await assignExternalChatNo(assignForm)
  ElMessage.success(t('assigned'))
  Object.assign(assignForm, { chatNo: undefined, receiverChatNo: undefined, dayNum: 30 })
  load()
}

getExternalProfile().then(res => profile.value = res.data || {})
load()
</script>

<style scoped>
.page { display:grid; grid-template-columns:minmax(480px,1.6fr) minmax(300px,.8fr); gap:16px; padding:20px; }.metric-grid { grid-column:1 / -1; display:flex; gap:16px; }.metric,.pool-panel,.assign-panel { background:#fff; border:1px solid #dcdfe6; border-radius:4px; padding:18px; }.metric { min-width:220px; }.metric span,.metric small { display:block; color:#909399; font-size:13px; }.metric strong { display:block; margin:8px 0; font-size:28px; color:#303133; }.pool-panel,.assign-panel { min-height:360px; }h2 { margin:0 0 18px; padding-left:10px; border-left:3px solid #409eff; font-size:16px; }@media(max-width:900px){.page{grid-template-columns:1fr}.metric-grid{flex-wrap:wrap}.metric{flex:1;min-width:180px}}
</style>
