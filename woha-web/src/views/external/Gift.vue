<template>
  <div class="page">
    <section class="summary">
      <div v-for="item in packs" :key="item.id" class="pack"><span>{{ item.giftName }}</span><strong>{{ quotaText(item.id) }}</strong><small>{{ item.status === '0' ? '可发放' : '已停用' }}</small></div>
    </section>
    <el-row :gutter="16">
      <el-col :md="10"><el-card><template #header>赠送礼包</template><el-form label-width="86px"><el-form-item label="用户 ID"><el-input v-model="form.receiverChatNo" /></el-form-item><el-form-item label="礼包"><el-select v-model="form.giftId" style="width:100%"><el-option v-for="item in enabledPacks" :key="item.id" :label="item.giftName" :value="item.id" /></el-select></el-form-item><el-form-item><el-button type="primary" :loading="issuing" @click="issue">赠送</el-button></el-form-item></el-form></el-card></el-col>
      <el-col :md="14"><el-card><template #header>赠送记录</template><el-table :data="issues"><el-table-column prop="createTime" label="时间" width="170" /><el-table-column prop="receiverUid" label="对象UID" /><el-table-column prop="giftName" label="礼包" /><el-table-column prop="contentSnapshot" label="内容" /><el-table-column label="状态"><template #default="{ row }"><el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? '已发放' : '失败' }}</el-tag></template></el-table-column></el-table></el-card></el-col>
    </el-row>
  </div>
</template>
<script setup>
import { issueExternalGiftPack, listExternalGiftPackIssues, listExternalGiftPackQuotas, listExternalGiftPacks } from '@/api/external/manager'
import { ElMessage } from 'element-plus'
const packs = ref([]); const quotas = ref([]); const issues = ref([]); const issuing = ref(false); const form = reactive({ receiverChatNo: '', giftId: null })
const enabledPacks = computed(() => packs.value.filter(item => item.status === '0'))
const quotaText = (giftId) => { const item = quotas.value.find(value => value.giftId === giftId); return item ? `${item.quotaCount - item.usedCount} / ${item.quotaCount}` : '0 / 0' }
async function load() { const [packRes, quotaRes, issueRes] = await Promise.all([listExternalGiftPacks({ status: '0' }), listExternalGiftPackQuotas(), listExternalGiftPackIssues()]); packs.value = packRes.data; quotas.value = quotaRes.data; issues.value = issueRes.data }
async function issue() { if (!form.receiverChatNo || !form.giftId) return ElMessage.warning('请填写用户 ID 并选择礼包'); issuing.value = true; try { await issueExternalGiftPack({ receiverChatNo: Number(form.receiverChatNo), giftId: form.giftId }); ElMessage.success('礼包已发放'); form.receiverChatNo = ''; await load() } finally { issuing.value = false } }
load()
</script>
<style scoped>.page{padding:20px}.summary{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:16px;margin-bottom:16px}.pack{display:grid;gap:6px;padding:16px;border:1px solid #dcdfe6;background:#fff}.pack strong{font-size:22px}.pack small{color:#909399}</style>
