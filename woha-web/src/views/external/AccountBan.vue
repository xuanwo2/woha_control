<template>
  <div class="page">
    <section class="authorization-panel">
      <h2>{{ t('myBanAuthorization') }}</h2>
      <div class="authorization-grid">
        <div><span>{{ t('executable') }}</span><strong>{{ canBan ? t('accountBan') : t('none') }}</strong></div>
        <div><span>{{ t('maximumDuration') }}</span><strong>{{ t('day7') }}</strong></div>
        <div><span>{{ t('dailyLimit') }}</span><strong>10</strong></div>
        <div><span>{{ t('targetScope') }}</span><strong>{{ t('teamOrGuildMembers') }}</strong></div>
      </div>
    </section>

    <section class="action-panel">
      <h2>{{ t('executeBan') }}</h2>
      <el-form :model="form" label-width="92px" class="ban-form">
        <el-form-item :label="t('userWohaId')"><el-input v-model.number="form.chatNo" :placeholder="t('enterUserWohaId')" /></el-form-item>
        <el-form-item :label="t('violationType')"><el-select v-model="form.violationType"><el-option v-for="item in violationTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item :label="t('duration')"><el-radio-group v-model="form.dayNum"><el-radio :label="1">{{ t('day1') }}</el-radio><el-radio :label="3">{{ t('day3') }}</el-radio><el-radio :label="7">{{ t('day7') }}</el-radio></el-radio-group></el-form-item>
        <el-form-item :label="t('reason')"><el-input v-model="form.reason" type="textarea" :rows="3" maxlength="200" show-word-limit :placeholder="t('enterProcessingDescription')" /></el-form-item>
        <el-form-item :label="t('evidenceImages')"><el-upload v-model:file-list="banEvidenceFiles" :action="uploadUrl" name="uploadFile" :headers="uploadHeaders" list-type="picture-card" accept="image/png,image/jpeg" :limit="3" :before-upload="beforeEvidenceUpload" :on-success="banEvidenceSuccess" :on-remove="removeBanEvidence" :on-exceed="evidenceExceed"><el-icon><Plus /></el-icon></el-upload></el-form-item>
        <el-form-item><el-button type="danger" :disabled="!canBan" @click="ban">{{ t('executeBan') }}</el-button></el-form-item>
      </el-form>
    </section>

    <section class="record-panel">
      <h2>{{ t('myBanRecords') }}</h2>
      <el-table :data="records" border size="small" :empty-text="t('noProcessingRecords')">
        <el-table-column prop="createTime" :label="t('time')" min-width="155" />
        <el-table-column :label="t('banType')" width="100"><template #default="{ row }">{{ row.operationType === 'BAN' ? t('executeBan') : t('releaseBan') }}</template></el-table-column>
        <el-table-column prop="target" :label="t('target')" width="120" />
        <el-table-column prop="content" :label="t('reason')" min-width="180" />
        <el-table-column :label="t('operation')" width="100" align="center"><template #default="{ row }"><el-button v-if="canBan && row.operationType === 'BAN'" link type="danger" @click="release(row)">{{ t('releaseBan') }}</el-button></template></el-table-column>
      </el-table>
    </section>

    <section v-if="canReport" class="report-panel">
      <h2>{{ t('submitReport') }}</h2>
      <el-form :model="reportForm" label-width="92px" class="ban-form">
        <el-form-item :label="t('reportedUserWohaId')"><el-input v-model.number="reportForm.targetChatNo" :placeholder="t('enterReportedUserWohaId')" /></el-form-item>
        <el-form-item :label="t('violationType')"><el-select v-model="reportForm.violationType"><el-option v-for="item in violationTypes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item :label="t('reportReason')"><el-input v-model="reportForm.description" type="textarea" :rows="3" maxlength="200" show-word-limit :placeholder="t('enterReportDescription')" /></el-form-item>
        <el-form-item :label="t('evidenceImages')"><el-upload v-model:file-list="evidenceFiles" :action="uploadUrl" name="uploadFile" :headers="uploadHeaders" list-type="picture-card" accept="image/png,image/jpeg" :limit="3" :before-upload="beforeEvidenceUpload" :on-success="reportEvidenceSuccess" :on-remove="removeReportEvidence" :on-exceed="evidenceExceed"><el-icon><Plus /></el-icon></el-upload></el-form-item>
        <el-form-item><el-button type="primary" @click="submitReport">{{ t('submitReport') }}</el-button></el-form-item>
      </el-form>
    </section>

    <section v-if="canReport" class="report-record-panel">
      <h2>{{ t('myReportRecords') }}</h2>
      <el-table :data="reportRecords" border size="small" :empty-text="t('noReportRecords')">
        <el-table-column prop="createTime" :label="t('submitTime')" min-width="155" />
        <el-table-column prop="targetChatNo" :label="t('target')" width="120" />
        <el-table-column prop="violationType" :label="t('violationType')" width="120" />
        <el-table-column prop="description" :label="t('reason')" min-width="180" show-overflow-tooltip />
        <el-table-column :label="t('status')" width="100"><template #default="{ row }"><el-tag :type="reportStatusType(row.status)">{{ reportStatusName(row.status) }}</el-tag></template></el-table-column>
        <el-table-column prop="processRemark" :label="t('processingReason')" min-width="160" show-overflow-tooltip />
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getExternalToken } from '@/utils/externalAuth'
import { banExternalAccount, getExternalProfile, listExternalAccountBanRecords, listExternalReports, releaseExternalAccountBan, submitExternalReport } from '@/api/external/manager'
import { useExternalI18n } from '@/i18n/external'

const { t } = useExternalI18n()
const form = reactive({ chatNo: undefined, dayNum: 1, violationType: '其他违规', reason: '', evidenceUrls: [] })
const profile = ref({})
const records = ref([])
const reportRecords = ref([])
const banEvidenceFiles = ref([])
const evidenceFiles = ref([])
const violationTypes = computed(() => [{ value: '广告引流', label: t('advertising') }, { value: '色情', label: t('pornography') }, { value: '欺诈', label: t('fraud') }, { value: '其他违规', label: t('otherViolation') }])
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
  if (!form.chatNo || !form.reason) return ElMessage.warning(t('enterUserAndReason'))
  await ElMessageBox.confirm(t('confirmBan').replace('{account}', form.chatNo).replace('{days}', form.dayNum), t('prompt'), {
    type: 'warning',
    confirmButtonText: t('confirm'),
    cancelButtonText: t('cancel')
  })
  await banExternalAccount(form)
  ElMessage.success(t('banSucceeded'))
  form.reason = ''
  form.evidenceUrls = []
  banEvidenceFiles.value = []
  loadRecords()
}

async function release(row) {
  await ElMessageBox.confirm(t('confirmUnban').replace('{account}', row.target), t('prompt'), {
    type: 'warning',
    confirmButtonText: t('confirm'),
    cancelButtonText: t('cancel')
  })
  await releaseExternalAccountBan(row.target)
  ElMessage.success(t('unbanned'))
  loadRecords()
}

function beforeEvidenceUpload(file) {
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error(t('evidenceSizeLimit'))
    return false
  }
  return true
}

function banEvidenceSuccess(response) {
  if (response.code !== 200 || !response.path) {
    ElMessage.error(response.msg || t('evidenceUploadFailed'))
    return
  }
  form.evidenceUrls.push(response.path)
}

function removeBanEvidence(file) {
  if (file.response?.path) form.evidenceUrls = form.evidenceUrls.filter(item => item !== file.response.path)
}

function reportEvidenceSuccess(response) {
  if (response.code !== 200 || !response.path) {
    ElMessage.error(response.msg || t('evidenceUploadFailed'))
    return
  }
  reportForm.evidenceUrls.push(response.path)
}

function removeReportEvidence(file) {
  if (file.response?.path) reportForm.evidenceUrls = reportForm.evidenceUrls.filter(item => item !== file.response.path)
}

function evidenceExceed() {
  ElMessage.warning(t('evidenceLimit'))
}

async function submitReport() {
  if (!reportForm.targetChatNo || !reportForm.description) return ElMessage.warning(t('enterReportedUserAndReason'))
  await submitExternalReport(reportForm)
  ElMessage.success(t('reportSubmitted'))
  Object.assign(reportForm, { targetChatNo: undefined, violationType: '其他违规', description: '', evidenceUrls: [] })
  evidenceFiles.value = []
  loadReportRecords()
}

const reportStatusName = status => ({ 0: t('pending'), 1: t('processed') })[status] || status
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
