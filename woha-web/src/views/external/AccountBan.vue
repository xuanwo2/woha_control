<template>
  <div class="page">
    <section class="authorization-panel">
      <h2>我的封禁授权</h2>
      <div class="authorization-grid">
        <div><span>可执行</span><strong>{{ canBan ? '账号封禁' : '无' }}</strong></div>
        <div><span>最长时长</span><strong>7 天</strong></div>
        <div><span>每日上限</span><strong>10 次</strong></div>
        <div><span>对象范围</span><strong>本团队下级或公会成员</strong></div>
      </div>
    </section>

    <section class="action-panel">
      <h2>执行封禁</h2>
      <el-form :model="form" label-width="92px" class="ban-form">
        <el-form-item label="用户 Woha ID"><el-input v-model.number="form.chatNo" placeholder="请输入用户 Woha ID" /></el-form-item>
        <el-form-item label="违规类型"><el-select v-model="form.violationType"><el-option v-for="item in violationTypes" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        <el-form-item label="时长"><el-radio-group v-model="form.dayNum"><el-radio :label="1">1 天</el-radio><el-radio :label="3">3 天</el-radio><el-radio :label="7">7 天</el-radio></el-radio-group></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.reason" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请输入处理说明" /></el-form-item>
        <el-form-item label="证据图片"><el-upload v-model:file-list="banEvidenceFiles" :action="uploadUrl" name="uploadFile" :headers="uploadHeaders" list-type="picture-card" accept="image/png,image/jpeg" :limit="3" :before-upload="beforeEvidenceUpload" :on-success="banEvidenceSuccess" :on-remove="removeBanEvidence" :on-exceed="evidenceExceed"><el-icon><Plus /></el-icon></el-upload></el-form-item>
        <el-form-item><el-button type="danger" :disabled="!canBan" @click="ban">执行封禁</el-button></el-form-item>
      </el-form>
    </section>

    <section class="record-panel">
      <h2>我的封禁记录</h2>
      <el-table :data="records" border size="small" empty-text="暂无处理记录">
        <el-table-column prop="createTime" label="时间" min-width="155" />
        <el-table-column label="类型" width="100"><template #default="{ row }">{{ row.operationType === 'BAN' ? '执行封禁' : '解除封禁' }}</template></el-table-column>
        <el-table-column prop="target" label="对象" width="120" />
        <el-table-column prop="content" label="说明" min-width="180" />
        <el-table-column label="操作" width="100" align="center"><template #default="{ row }"><el-button v-if="canBan && row.operationType === 'BAN'" link type="danger" @click="release(row)">解除</el-button></template></el-table-column>
      </el-table>
    </section>

    <section v-if="canReport" class="report-panel">
      <h2>提交举报</h2>
      <el-form :model="reportForm" label-width="92px" class="ban-form">
        <el-form-item label="用户 Woha ID"><el-input v-model.number="reportForm.targetChatNo" placeholder="请输入被举报用户 Woha ID" /></el-form-item>
        <el-form-item label="违规类型"><el-select v-model="reportForm.violationType"><el-option v-for="item in violationTypes" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        <el-form-item label="举报说明"><el-input v-model="reportForm.description" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请输入举报说明" /></el-form-item>
        <el-form-item label="证据图片"><el-upload v-model:file-list="evidenceFiles" :action="uploadUrl" name="uploadFile" :headers="uploadHeaders" list-type="picture-card" accept="image/png,image/jpeg" :limit="3" :before-upload="beforeEvidenceUpload" :on-success="reportEvidenceSuccess" :on-remove="removeReportEvidence" :on-exceed="evidenceExceed"><el-icon><Plus /></el-icon></el-upload></el-form-item>
        <el-form-item><el-button type="primary" @click="submitReport">提交举报</el-button></el-form-item>
      </el-form>
    </section>

    <section v-if="canReport" class="report-record-panel">
      <h2>我的举报记录</h2>
      <el-table :data="reportRecords" border size="small" empty-text="暂无举报记录">
        <el-table-column prop="createTime" label="提交时间" min-width="155" />
        <el-table-column prop="targetChatNo" label="对象" width="120" />
        <el-table-column prop="violationType" label="违规类型" width="120" />
        <el-table-column prop="description" label="说明" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="reportStatusType(row.status)">{{ reportStatusName(row.status) }}</el-tag></template></el-table-column>
        <el-table-column prop="processRemark" label="处理说明" min-width="160" show-overflow-tooltip />
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getExternalToken } from '@/utils/externalAuth'
import { banExternalAccount, getExternalProfile, listExternalAccountBanRecords, listExternalReports, releaseExternalAccountBan, submitExternalReport } from '@/api/external/manager'

const form = reactive({ chatNo: undefined, dayNum: 1, violationType: '其他违规', reason: '', evidenceUrls: [] })
const profile = ref({})
const records = ref([])
const reportRecords = ref([])
const banEvidenceFiles = ref([])
const evidenceFiles = ref([])
const violationTypes = ['广告引流', '色情', '欺诈', '其他违规']
const reportForm = reactive({ targetChatNo: undefined, violationType: '其他违规', description: '', evidenceUrls: [] })
const canBan = computed(() => String(profile.value.permissions || '').split(',').includes('EXECUTE_BAN'))
const canReport = computed(() => String(profile.value.permissions || '').split(',').includes('SUBMIT_REPORT'))
const uploadUrl = `${import.meta.env.VITE_APP_BASE_API}/upload/img.action`
const uploadHeaders = computed(() => ({ 'X-External-Token': getExternalToken() }))

async function loadRecords() {
  const result = await listExternalAccountBanRecords()
  records.value = result.data || []
}

async function loadReportRecords() {
  const result = await listExternalReports()
  reportRecords.value = result.data || []
}

async function ban() {
  if (!form.chatNo || !form.reason) return ElMessage.warning('请填写用户 Woha ID 和处理说明')
  await ElMessageBox.confirm(`确认封禁账号 ${form.chatNo} ${form.dayNum} 天？`, '提示', { type: 'warning' })
  await banExternalAccount(form)
  ElMessage.success('封禁成功')
  form.reason = ''
  form.evidenceUrls = []
  banEvidenceFiles.value = []
  loadRecords()
}

async function release(row) {
  await ElMessageBox.confirm(`确认解除账号 ${row.target} 的封禁？`, '提示', { type: 'warning' })
  await releaseExternalAccountBan(row.target)
  ElMessage.success('已解除封禁')
  loadRecords()
}

function beforeEvidenceUpload(file) {
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('证据图片不能超过5MB')
    return false
  }
  return true
}

function banEvidenceSuccess(response) {
  if (response.code !== 200 || !response.path) {
    ElMessage.error(response.msg || '证据图片上传失败')
    return
  }
  form.evidenceUrls.push(response.path)
}

function removeBanEvidence(file) {
  if (file.response?.path) form.evidenceUrls = form.evidenceUrls.filter(item => item !== file.response.path)
}

function reportEvidenceSuccess(response) {
  if (response.code !== 200 || !response.path) {
    ElMessage.error(response.msg || '证据图片上传失败')
    return
  }
  reportForm.evidenceUrls.push(response.path)
}

function removeReportEvidence(file) {
  if (file.response?.path) reportForm.evidenceUrls = reportForm.evidenceUrls.filter(item => item !== file.response.path)
}

function evidenceExceed() {
  ElMessage.warning('证据图片最多上传3张')
}

async function submitReport() {
  if (!reportForm.targetChatNo || !reportForm.description) return ElMessage.warning('请填写被举报用户 Woha ID 和举报说明')
  await submitExternalReport(reportForm)
  ElMessage.success('举报已提交')
  Object.assign(reportForm, { targetChatNo: undefined, violationType: '其他违规', description: '', evidenceUrls: [] })
  evidenceFiles.value = []
  loadReportRecords()
}

const reportStatusName = status => ({ 0: '待处理', 1: '已处理' })[status] || status
const reportStatusType = status => ({ 0: 'warning', 1: 'success' })[status] || 'info'

async function init() {
  const result = await getExternalProfile()
  profile.value = result.data || {}
  if (canBan.value) loadRecords()
  if (canReport.value) loadReportRecords()
}

init()
</script>

<style scoped>
.page { display:grid; grid-template-columns:minmax(360px,.95fr) minmax(460px,1.05fr); gap:16px; padding:20px; }.authorization-panel { grid-column:1 / -1; }.authorization-panel,.action-panel,.record-panel,.report-panel,.report-record-panel { background:#fff; border:1px solid #dcdfe6; border-radius:4px; padding:18px; }h2 { margin:0 0 18px; padding-left:10px; border-left:3px solid #409eff; font-size:16px; }.authorization-grid { display:grid; grid-template-columns:repeat(4,1fr); gap:20px; }.authorization-grid span { display:block; color:#909399; font-size:13px; }.authorization-grid strong { display:block; margin-top:8px; color:#303133; }.ban-form { max-width:460px; }@media(max-width:1000px){.page{grid-template-columns:1fr}.authorization-grid{grid-template-columns:repeat(2,1fr)}}
</style>
