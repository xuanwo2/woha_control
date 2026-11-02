<template>
  <div class="page">
    <section class="quota-grid">
      <div v-for="item in configuredPacks" :key="item.id" class="quota-card"><span>{{ item.giftName }} · {{ t('quotaRemaining') }}</span><strong>{{ quotaText(item.id) }}</strong><small>{{ t('perPersonLimit').replace('{count}', item.receiveLimit || 0) }}</small></div>
    </section>
    <section v-if="canIssue" class="issue-panel">
      <h2>{{ t('giveGiftPack') }}</h2>
      <el-form label-width="86px" class="issue-form">
        <el-form-item :label="t('linkedWohaId')"><el-input v-model.number="form.receiverChatNo" :placeholder="t('enter')" /></el-form-item>
        <el-form-item :label="t('giftPack')"><el-select v-model="form.giftId" :placeholder="t('selectGiftPack')"><el-option v-for="item in configuredPacks" :key="item.id" :label="packOptionLabel(item)" :value="item.id" :disabled="item.quotaCount <= item.usedCount" /></el-select></el-form-item>
        <el-form-item :label="t('content')"><span class="content-text">{{ selectedPack?.content || selectedPack?.giftName || '--' }}</span></el-form-item>
        <el-form-item><el-button type="primary" :loading="issuing" @click="issue">{{ t('give') }}</el-button></el-form-item>
      </el-form>
    </section>
    <section class="record-panel">
      <h2>{{ t('giftRecords') }}</h2>
      <el-table :data="issues" border size="small" :empty-text="t('noOperations')">
        <el-table-column prop="createTime" :label="t('time')" min-width="155" />
        <el-table-column prop="receiverChatNo" :label="t('target')" min-width="105" />
        <el-table-column prop="giftName" :label="t('giftPack')" min-width="120" />
        <el-table-column prop="loginName" :label="t('operator')" min-width="110" />
        <el-table-column :label="t('status')" width="100"><template #default="{ row }"><el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? t('issued') : t('failed') }}</el-tag></template></el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { getExternalProfile, issueExternalGiftPack, listExternalGiftPackIssues, listExternalGiftPackQuotas, listExternalGiftPacks } from '@/api/external/manager'
import { useExternalI18n } from '@/i18n/external'

const packs = ref([])
const { t } = useExternalI18n()
const quotas = ref([])
const issues = ref([])
const issuing = ref(false)
const profile = ref({})
const form = reactive({ receiverChatNo: undefined, giftId: undefined })
const configuredPacks = computed(() => packs.value.map(pack => {
  const quota = quotas.value.find(item => item.giftId === pack.id)
  return {
    ...pack,
    quotaCount: quota?.quotaCount || 0,
    usedCount: quota?.usedCount || 0
  }
}))
const selectedPack = computed(() => configuredPacks.value.find(item => item.id === form.giftId))
const canIssue = computed(() => String(profile.value.permissions || '').split(',').includes('SEND_GIFT_PACK'))
const quotaText = giftId => { const item = configuredPacks.value.find(value => value.id === giftId); return item ? `${Math.max(0, item.quotaCount - item.usedCount)} / ${item.quotaCount}` : '0 / 0' }
const packOptionLabel = item => item.quotaCount <= item.usedCount ? `${item.giftName} (${t('quotaInsufficient')})` : item.giftName

async function load() {
  const [packRes, quotaRes, issueRes] = await Promise.all([listExternalGiftPacks({ status: '0' }), listExternalGiftPackQuotas(), listExternalGiftPackIssues()])
  packs.value = packRes.data || []
  quotas.value = quotaRes.data || []
  issues.value = issueRes.data || []
}

async function issue() {
  if (!form.receiverChatNo || !form.giftId) return ElMessage.warning(t('fillUserAndGift'))
  if (!selectedPack.value || selectedPack.value.quotaCount <= selectedPack.value.usedCount) return ElMessage.warning(t('insufficientQuota'))
  issuing.value = true
  try {
    await issueExternalGiftPack(form)
    ElMessage.success(t('giftIssued'))
    Object.assign(form, { receiverChatNo: undefined, giftId: undefined })
    load()
  } finally {
    issuing.value = false
  }
}

getExternalProfile().then(res => profile.value = res.data || {})
load()
</script>

<style scoped>
.page { display:grid; grid-template-columns:minmax(340px,.9fr) minmax(480px,1.1fr); gap:16px; padding:20px; }.quota-grid { grid-column:1 / -1; display:grid; grid-template-columns:repeat(auto-fit,minmax(220px,1fr)); gap:16px; }.quota-card,.issue-panel,.record-panel { background:#fff; border:1px solid #dcdfe6; border-radius:4px; padding:18px; }.quota-card span,.quota-card small { display:block; color:#909399; font-size:13px; }.quota-card strong { display:block; margin:8px 0; font-size:26px; color:#303133; }h2 { margin:0 0 18px; padding-left:10px; border-left:3px solid #409eff; font-size:16px; }.issue-form { max-width:420px; }.content-text { color:#606266; }.record-panel,.issue-panel { min-height:340px; }@media(max-width:900px){.page{grid-template-columns:1fr}}
</style>
