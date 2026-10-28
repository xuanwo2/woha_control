<template>
  <div class="page">
    <el-form :model="query" inline>
      <el-form-item label="公会ID/名称"><el-input v-model="query.keyword" clearable placeholder="请输入" @keyup.enter="load" /></el-form-item>
      <el-form-item label="负责人"><el-select v-model="query.responsibleWohaUid" clearable placeholder="请选择"><el-option v-for="item in responsibles" :key="item.wohaUid" :label="responsibleLabel(item)" :value="item.wohaUid" /></el-select></el-form-item>
      <el-form-item label="创建方式"><el-select v-model="query.createSource" clearable placeholder="请选择"><el-option label="外管创建" :value="0" /><el-option label="入驻申请" :value="1" /></el-select></el-form-item>
      <el-button type="primary" @click="load">查询</el-button><el-button @click="reset">重置</el-button>
    </el-form>
    <div class="toolbar"><h2>公会管理</h2><div><el-button @click="exportData">导出</el-button><el-button type="primary" @click="openCreate">新建公会</el-button></div></div>
    <el-table :data="rows" v-loading="loading" border>
      <el-table-column prop="familyName" label="公会" min-width="180"><template #default="{ row }">{{ row.familyName }}（{{ row.familyNo }}）</template></el-table-column>
      <el-table-column prop="captainChatNo" label="会长ID" min-width="110" />
      <el-table-column prop="captainNickname" label="会长昵称" min-width="120" />
      <el-table-column prop="responsibleName" label="负责人" min-width="110" />
      <el-table-column prop="bdLeaderName" label="所属BD Leader" min-width="130" />
      <el-table-column prop="anchorCount" label="主播数" width="90" />
      <el-table-column prop="monthFlow" label="本月流水（金币）" min-width="150"><template #default="{ row }">{{ number(row.monthFlow) }}</template></el-table-column>
      <el-table-column prop="createMethod" label="创建方式" width="110" />
      <el-table-column prop="statusName" label="状态" width="100" />
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />

    <el-drawer v-model="visible" title="新建公会" size="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="115px">
        <el-form-item label="会长 Woha ID" prop="captainChatNo"><el-input v-model.number="form.captainChatNo" @blur="loadCaptainPreview" /></el-form-item>
        <el-form-item v-if="captainPreview" label="会长昵称"><span>{{ captainPreviewText }}</span></el-form-item>
        <el-form-item label="公会名称" prop="familyName"><el-input v-model="form.familyName" maxlength="30" show-word-limit /></el-form-item>
        <el-form-item label="公会封面"><el-upload :action="uploadUrl" name="uploadFile" :headers="uploadHeaders" :show-file-list="false" accept="image/png,image/jpeg" :before-upload="beforeUpload" :on-success="uploadSuccess"><el-button>上传图片</el-button></el-upload><el-image v-if="form.familyLogo" :src="form.familyLogo" fit="cover" class="cover" /></el-form-item>
        <el-form-item label="会长 WhatsApp"><el-input v-model="form.whatsapp" /></el-form-item>
        <el-form-item label="负责人" prop="responsibleAccountId"><el-select v-model="form.responsibleAccountId" placeholder="请选择负责人" class="full"><el-option v-for="item in responsibles" :key="item.id" :label="responsibleLabel(item)" :value="item.id" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="submit">创建</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { getExternalToken } from '@/utils/externalAuth'
import { createExternalGuild, getExternalGuildCaptainPreview, listExternalGuild, listExternalGuildResponsibles } from '@/api/external/manager'

const { proxy } = getCurrentInstance()
const rows = ref([]), total = ref(0), loading = ref(false), visible = ref(false), submitting = ref(false), formRef = ref()
const responsibles = ref([])
const captainPreview = ref(null)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', responsibleWohaUid: undefined, createSource: undefined })
const form = reactive({ captainChatNo: undefined, familyName: '', familyLogo: '', whatsapp: '', responsibleAccountId: undefined })
const rules = { captainChatNo: [{ required: true, message: '请输入会长 Woha ID', trigger: 'blur' }], familyName: [{ required: true, message: '请输入公会名称', trigger: 'blur' }], responsibleAccountId: [{ required: true, message: '请选择负责人', trigger: 'change' }] }
const uploadUrl = `${import.meta.env.VITE_APP_BASE_API}/upload/img.action`
const uploadHeaders = computed(() => ({ 'X-External-Token': getExternalToken() }))
const number = value => Number(value || 0).toLocaleString()
const responsibleLabel = item => `${item.realName || item.loginName}（${item.wohaChatNo || item.wohaUid}）`
const captainPreviewText = computed(() => {
  if (!captainPreview.value.registered) return '未注册'
  if (!captainPreview.value.inCurrentRegion) return `${captainPreview.value.nickname || '--'}（已注册 · 不属于当前大区）`
  return `${captainPreview.value.nickname || '--'}（已注册 · ${captainPreview.value.joinedFamily ? '已加入公会' : '未加入公会'}）`
})

async function loadResponsibles() { const res = await listExternalGuildResponsibles(); responsibles.value = res.data || [] }
async function load() { loading.value = true; try { const res = await listExternalGuild(query); rows.value = res.rows || []; total.value = res.total || 0 } finally { loading.value = false } }
function reset() { Object.assign(query, { pageNum: 1, keyword: '', responsibleWohaUid: undefined, createSource: undefined }); load() }
async function openCreate() { Object.assign(form, { captainChatNo: undefined, familyName: '', familyLogo: '', whatsapp: '', responsibleAccountId: undefined }); captainPreview.value = null; await loadResponsibles(); visible.value = true }
async function loadCaptainPreview() { if (!form.captainChatNo) { captainPreview.value = null; return }; const chatNo = form.captainChatNo; const res = await getExternalGuildCaptainPreview(chatNo); if (form.captainChatNo === chatNo) captainPreview.value = res.data }
function beforeUpload(file) { if (file.size / 1024 / 1024 > 5) { ElMessage.error('封面图片不能超过5MB'); return false } return true }
function uploadSuccess(res) { if (res.code !== 200 || !res.path) { ElMessage.error(res.msg || '图片上传失败'); return } form.familyLogo = res.path }
function submit() { formRef.value.validate(async valid => { if (!valid) return; submitting.value = true; try { await createExternalGuild(form); ElMessage.success('公会创建成功'); visible.value = false; load() } finally { submitting.value = false } }) }
function exportData() { proxy.download('external/guild/export', { keyword: query.keyword, responsibleWohaUid: query.responsibleWohaUid, createSource: query.createSource }, `公会数据_${Date.now()}.xlsx`) }
loadResponsibles(); load()
</script>

<style scoped>.page{padding:20px}.toolbar{display:flex;justify-content:space-between;align-items:center;margin:0 0 16px}.toolbar h2{font-size:18px;margin:0}.cover{display:block;width:120px;height:70px;margin-top:10px;border:1px solid #dcdfe6}.full{width:100%}</style>
