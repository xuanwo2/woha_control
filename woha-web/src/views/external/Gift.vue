<template>
  <div class="page">
    <section class="quota-grid">
      <div v-for="item in configuredPacks" :key="item.id" class="quota-card"><span>{{ item.giftName }} · 剩余 / 额度</span><strong>{{ quotaText(item.id) }}</strong><small>每人限领 {{ item.receiveLimit || 0 }} 次</small></div>
    </section>
    <section class="issue-panel">
      <h2>赠送礼包</h2>
      <el-form label-width="86px" class="issue-form">
        <el-form-item label="用户 Woha ID"><el-input v-model.number="form.receiverChatNo" placeholder="请输入用户 Woha ID" /></el-form-item>
        <el-form-item label="礼包"><el-select v-model="form.giftId" placeholder="请选择礼包"><el-option v-for="item in configuredPacks" :key="item.id" :label="packOptionLabel(item)" :value="item.id" :disabled="item.quotaCount <= item.usedCount" /></el-select></el-form-item>
        <el-form-item label="内容"><span class="content-text">{{ selectedPack?.content || selectedPack?.giftName || '--' }}</span></el-form-item>
        <el-form-item><el-button type="primary" :loading="issuing" @click="issue">赠送</el-button></el-form-item>
      </el-form>
    </section>
    <section class="record-panel">
      <h2>赠送记录</h2>
      <el-table :data="issues" border size="small" empty-text="暂无赠送记录">
        <el-table-column prop="createTime" label="时间" min-width="155" />
        <el-table-column prop="receiverUid" label="对象" min-width="105" />
        <el-table-column prop="giftName" label="礼包" min-width="120" />
        <el-table-column prop="loginName" label="操作人" min-width="110" />
        <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? '已发放' : '失败' }}</el-tag></template></el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { issueExternalGiftPack, listExternalGiftPackIssues, listExternalGiftPackQuotas, listExternalGiftPacks } from '@/api/external/manager'

const packs = ref([])
const quotas = ref([])
const issues = ref([])
const issuing = ref(false)
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
const quotaText = giftId => { const item = configuredPacks.value.find(value => value.id === giftId); return item ? `${Math.max(0, item.quotaCount - item.usedCount)} / ${item.quotaCount}` : '0 / 0' }
const packOptionLabel = item => item.quotaCount <= item.usedCount ? `${item.giftName}（额度不足）` : item.giftName

async function load() {
  const [packRes, quotaRes, issueRes] = await Promise.all([listExternalGiftPacks({ status: '0' }), listExternalGiftPackQuotas(), listExternalGiftPackIssues()])
  packs.value = packRes.data || []
  quotas.value = quotaRes.data || []
  issues.value = issueRes.data || []
}

async function issue() {
  if (!form.receiverChatNo || !form.giftId) return ElMessage.warning('请填写用户 Woha ID 并选择礼包')
  if (!selectedPack.value || selectedPack.value.quotaCount <= selectedPack.value.usedCount) return ElMessage.warning('当前礼包额度不足，请在管理后台为该外管账号配置可赠份数')
  issuing.value = true
  try {
    await issueExternalGiftPack(form)
    ElMessage.success('礼包已发放')
    Object.assign(form, { receiverChatNo: undefined, giftId: undefined })
    load()
  } finally {
    issuing.value = false
  }
}

load()
</script>

<style scoped>
.page { display:grid; grid-template-columns:minmax(340px,.9fr) minmax(480px,1.1fr); gap:16px; padding:20px; }.quota-grid { grid-column:1 / -1; display:grid; grid-template-columns:repeat(auto-fit,minmax(220px,1fr)); gap:16px; }.quota-card,.issue-panel,.record-panel { background:#fff; border:1px solid #dcdfe6; border-radius:4px; padding:18px; }.quota-card span,.quota-card small { display:block; color:#909399; font-size:13px; }.quota-card strong { display:block; margin:8px 0; font-size:26px; color:#303133; }h2 { margin:0 0 18px; padding-left:10px; border-left:3px solid #409eff; font-size:16px; }.issue-form { max-width:420px; }.content-text { color:#606266; }.record-panel,.issue-panel { min-height:340px; }@media(max-width:900px){.page{grid-template-columns:1fr}}
</style>
