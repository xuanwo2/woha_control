<template>
  <div class="page">
    <section class="export-panel">
      <h2>新建导出</h2>
      <el-form label-width="86px">
        <el-form-item label="数据集">
          <el-radio-group v-model="selectedCode" @change="changeDataset">
            <el-radio v-for="item in datasets" :key="item.code" :label="item.code">{{ item.name }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="范围"><span class="scope-text">我的团队及下级团队</span></el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        </el-form-item>
        <el-form-item label="可导出字段">
          <el-checkbox-group v-model="selectedFields" class="field-list">
            <el-checkbox v-for="field in currentDataset.fields" :key="field.code" :label="field.code">{{ field.name }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item><el-button type="primary" :disabled="!selectedCode || !selectedFields.length" @click="exportData">导出</el-button></el-form-item>
      </el-form>
    </section>

    <section class="record-panel">
      <h2>导出记录</h2>
      <el-table :data="records" border size="small" empty-text="暂无导出记录">
        <el-table-column prop="time" label="时间" min-width="155" />
        <el-table-column prop="datasetName" label="数据集" min-width="120" />
        <el-table-column prop="fieldCount" label="字段数" width="90" align="center" />
        <el-table-column prop="operator" label="操作人" min-width="110" />
        <el-table-column label="下载" width="100" align="center"><template #default="{ row }"><el-button link type="primary" @click="downloadRecord(row)">下载</el-button></template></el-table-column>
      </el-table>
      <p class="record-tip">导出记录保留 7 天；单次导出最多 10000 行。</p>
    </section>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { getExternalProfile } from '@/api/external/manager'

const { proxy } = getCurrentInstance()
const today = new Date().toISOString().slice(0, 10)
const storageKey = 'externalExportRecords'
const dateRange = ref([today, today])
const profile = ref({})
const selectedCode = ref('')
const selectedFields = ref([])
const records = ref([])
const datasetDefinitions = [
  { code: 'GUILD_LIST', permission: 'EXPORT_GUILD_LIST', name: '公会列表', path: 'external/guild/list/export', fields: [{ code: 'familyName', name: '公会名称' }, { code: 'familyNo', name: '公会ID' }, { code: 'captainChatNo', name: '会长ID' }, { code: 'captainNickname', name: '会长昵称' }, { code: 'responsibleName', name: '负责人' }, { code: 'bdLeaderName', name: '所属BD Leader' }, { code: 'anchorCount', name: '主播数' }, { code: 'monthFlow', name: '本月流水' }, { code: 'createMethod', name: '创建方式' }, { code: 'statusName', name: '状态' }, { code: 'createTime', name: '创建时间' }] },
  { code: 'GUILD_FLOW', permission: 'EXPORT_GUILD_FLOW', name: '公会流水', path: 'external/guild/export', fields: [{ code: 'familyName', name: '公会名称' }, { code: 'familyNo', name: '公会ID' }, { code: 'responsibleName', name: '负责人' }, { code: 'bdLeaderName', name: '所属BD Leader' }, { code: 'rangeFlow', name: '范围流水' }, { code: 'weekFlow', name: '周流水' }, { code: 'monthFlow', name: '月流水' }, { code: 'totalFlow', name: '总流水' }, { code: 'createTime', name: '创建时间' }] },
  { code: 'TEAM_MEMBER', permission: 'EXPORT_TEAM_MEMBER', name: '团队成员', path: 'external/team/members/export', fields: [{ code: 'loginName', name: '外管账号' }, { code: 'realName', name: '姓名' }, { code: 'wohaUid', name: '关联Woha UID' }, { code: 'roleCode', name: '角色' }, { code: 'parentLoginName', name: '上级账号' }] },
  { code: 'GIFT_ISSUE', permission: 'EXPORT_GIFT_ISSUE', name: '礼包赠送记录', path: 'external/gift-packs/issues/export', fields: [{ code: 'giftName', name: '礼包名称' }, { code: 'receiverUid', name: '接收用户UID' }, { code: 'loginName', name: '操作账号' }, { code: 'contentSnapshot', name: '发放内容' }, { code: 'status', name: '状态' }, { code: 'createTime', name: '发放时间' }] }
]
const datasets = computed(() => {
  const permissions = String(profile.value.permissions || '').split(',')
  return datasetDefinitions.filter(item => permissions.includes(item.permission))
})
const currentDataset = computed(() => datasets.value.find(item => item.code === selectedCode.value) || { fields: [] })

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
  records.value.unshift({ id: `${Date.now()}-${dataset.code}`, timestamp: Date.now(), time: new Date().toLocaleString('zh-CN', { hour12: false }), datasetName: dataset.name, fieldCount: params.fieldCodes.split(',').length, operator: profile.value.realName || profile.value.loginName, code: dataset.code, params })
  localStorage.setItem(storageKey, JSON.stringify(records.value))
}

async function download(dataset, params) {
  return await proxy.download(dataset.path, params, `${dataset.name}_${Date.now()}.xlsx`)
}

async function exportData() {
  const dataset = currentDataset.value
  const params = { fieldCodes: selectedFields.value.join(','), startDate: dateRange.value?.[0], endDate: dateRange.value?.[1] }
  if (!await download(dataset, params)) return
  saveRecord(dataset, params)
  ElMessage.success('已开始导出')
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
