<template>
  <div class="page">
    <section class="export-panel">
      <h2>{{ t('newExport') }}</h2>
      <el-form label-width="86px">
        <el-form-item :label="t('dataset')">
          <el-radio-group v-model="selectedCode" @change="changeDataset">
            <el-radio v-for="item in datasets" :key="item.code" :label="item.code">{{ datasetName(item) }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('scope')"><span class="scope-text">{{ t('myTeamAndSubteams') }}</span></el-form-item>
        <el-form-item :label="t('date')">
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" :range-separator="t('rangeSeparator')" :start-placeholder="t('startDate')" :end-placeholder="t('endDate')" />
        </el-form-item>
        <el-form-item :label="t('exportableFields')">
          <el-checkbox-group v-model="selectedFields" class="field-list">
            <el-checkbox v-for="field in currentDataset.fields" :key="field.code" :label="field.code">{{ fieldName(field) }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item><el-button type="primary" :disabled="!selectedCode || !selectedFields.length" @click="exportData">{{ t('export') }}</el-button></el-form-item>
      </el-form>
    </section>

    <section class="record-panel">
      <h2>{{ t('exportRecords') }}</h2>
      <el-table :data="records" border size="small" :empty-text="t('noExportRecords')">
        <el-table-column prop="time" :label="t('time')" min-width="155" />
        <el-table-column :label="t('dataset')" min-width="120"><template #default="{ row }">{{ recordDatasetName(row) }}</template></el-table-column>
        <el-table-column prop="fieldCount" :label="t('fieldCount')" width="90" align="center" />
        <el-table-column prop="operator" :label="t('operator')" min-width="110" />
        <el-table-column :label="t('download')" width="100" align="center"><template #default="{ row }"><el-button link type="primary" @click="downloadRecord(row)">{{ t('download') }}</el-button></template></el-table-column>
      </el-table>
      <p class="record-tip">{{ t('exportRecordTip') }}</p>
    </section>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { getExternalProfile } from '@/api/external/manager'
import { useExternalI18n } from '@/i18n/external'

const { proxy } = getCurrentInstance()
const { locale, t } = useExternalI18n()
const today = new Date().toISOString().slice(0, 10)
const storageKey = 'externalExportRecords'
const dateRange = ref([today, today])
const profile = ref({})
const selectedCode = ref('')
const selectedFields = ref([])
const records = ref([])
const datasetDefinitions = [
  { code: 'GUILD_LIST', permission: 'EXPORT_GUILD_LIST', nameKey: 'guildList', path: 'external/guild/list/export', fields: [{ code: 'familyName', nameKey: 'guildName' }, { code: 'familyNo', nameKey: 'guildId' }, { code: 'captainChatNo', nameKey: 'captainId' }, { code: 'captainNickname', nameKey: 'captainNickname' }, { code: 'responsibleName', nameKey: 'responsible' }, { code: 'bdLeaderName', nameKey: 'bdLeader' }, { code: 'anchorCount', nameKey: 'anchorCount' }, { code: 'monthFlow', nameKey: 'monthlyFlow' }, { code: 'createMethod', nameKey: 'createMethod' }, { code: 'statusName', nameKey: 'status' }, { code: 'createTime', nameKey: 'createTime' }] },
  { code: 'GUILD_FLOW', permission: 'EXPORT_GUILD_FLOW', nameKey: 'guildFlow', path: 'external/guild/export', fields: [{ code: 'familyName', nameKey: 'guildName' }, { code: 'familyNo', nameKey: 'guildId' }, { code: 'responsibleName', nameKey: 'responsible' }, { code: 'bdLeaderName', nameKey: 'bdLeader' }, { code: 'rangeFlow', nameKey: 'rangeFlow' }, { code: 'weekFlow', nameKey: 'weeklyFlow' }, { code: 'monthFlow', nameKey: 'monthlyFlow' }, { code: 'totalFlow', nameKey: 'totalFlow' }, { code: 'createTime', nameKey: 'createTime' }] },
  { code: 'TEAM_MEMBER', permission: 'EXPORT_TEAM_MEMBER', nameKey: 'teamMembers', path: 'external/team/members/export', fields: [{ code: 'loginName', nameKey: 'externalAccount' }, { code: 'realName', nameKey: 'name' }, { code: 'wohaUid', nameKey: 'linkedWohaId' }, { code: 'roleCode', nameKey: 'role' }, { code: 'parentLoginName', nameKey: 'parentAccount' }] },
  { code: 'GIFT_ISSUE', permission: 'EXPORT_GIFT_ISSUE', nameKey: 'giftIssueRecords', path: 'external/gift-packs/issues/export', fields: [{ code: 'giftName', nameKey: 'giftName' }, { code: 'receiverUid', nameKey: 'receiverId' }, { code: 'loginName', nameKey: 'externalAccount' }, { code: 'contentSnapshot', nameKey: 'issueContent' }, { code: 'status', nameKey: 'status' }, { code: 'createTime', nameKey: 'issueTime' }] }
]
const datasets = computed(() => {
  const permissions = String(profile.value.permissions || '').split(',')
  return datasetDefinitions.filter(item => permissions.includes(item.permission))
})
const currentDataset = computed(() => datasets.value.find(item => item.code === selectedCode.value) || { fields: [] })
const datasetName = dataset => t(dataset.nameKey)
const fieldName = field => t(field.nameKey)
const recordDatasetName = record => datasetName(datasetDefinitions.find(item => item.code === record.code) || { nameKey: 'dataset' })

function changeDataset() {
  selectedFields.value = currentDataset.value.fields.map(item => item.code)
}

function loadRecords() {
  const now = Date.now()
  const saved = JSON.parse(localStorage.getItem(storageKey) || '[]')
  records.value = saved.filter(item => now - item.timestamp < 7 * 24 * 60 * 60 * 1000)
  localStorage.setItem(storageKey, JSON.stringify(records.value))
}

function saveRecord(dataset, params) {
  records.value.unshift({ id: `${Date.now()}-${dataset.code}`, timestamp: Date.now(), time: new Date().toLocaleString(locale.value, { hour12: false }), datasetName: datasetName(dataset), fieldCount: params.fieldCodes.split(',').length, operator: profile.value.realName || profile.value.loginName, code: dataset.code, params })
  localStorage.setItem(storageKey, JSON.stringify(records.value))
}

async function download(dataset, params) {
  return await proxy.download(dataset.path, params, `${datasetName(dataset)}_${Date.now()}.xlsx`)
}

async function exportData() {
  const dataset = currentDataset.value
  const params = { fieldCodes: selectedFields.value.join(','), startDate: dateRange.value?.[0], endDate: dateRange.value?.[1] }
  if (!await download(dataset, params)) return
  saveRecord(dataset, params)
  ElMessage.success(t('exportStarted'))
}

async function downloadRecord(record) {
  const dataset = datasetDefinitions.find(item => item.code === record.code)
  if (!dataset) return
  await download(dataset, record.params)
}

async function init() {
  const result = await getExternalProfile()
  profile.value = result.data || {}
  loadRecords()
  if (datasets.value.length) {
    selectedCode.value = datasets.value[0].code
    changeDataset()
  }
}

init()
</script>

<style scoped>
.page { display:grid; grid-template-columns:minmax(420px,1.05fr) minmax(420px,.95fr); gap:16px; padding:20px; }
.export-panel,.record-panel { min-height:520px; padding:18px; background:#fff; border:1px solid #dcdfe6; border-radius:4px; }
h2 { margin:0 0 20px; padding-left:10px; border-left:3px solid #409eff; font-size:16px; }
.scope-text { color:#606266; }.field-list { display:grid; grid-template-columns:repeat(3,minmax(120px,1fr)); gap:12px 18px; width:100%; }.record-tip { margin-top:20px; color:#909399; font-size:13px; }
@media (max-width:1000px) { .page { grid-template-columns:1fr; }.export-panel,.record-panel { min-height:auto; }.field-list { grid-template-columns:repeat(2,minmax(120px,1fr)); } }
</style>
