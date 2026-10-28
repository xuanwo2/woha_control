<template>
  <div class="app-container">
    <el-form :model="query" inline>
      <el-form-item label="大区"><el-select v-model="query.regionId" clearable placeholder="请选择"><el-option v-for="item in regions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
      <el-form-item label="外管账号"><el-input v-model="query.loginName" clearable placeholder="请输入" @keyup.enter="search" /></el-form-item>
      <el-form-item label="所属超级管理员"><el-input v-model="query.superAdminName" clearable placeholder="请输入" @keyup.enter="search" /></el-form-item>
      <el-form-item label="操作类型"><el-select v-model="query.operationType" clearable placeholder="请选择"><el-option v-for="item in types" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
      <el-form-item label="时间"><el-date-picker v-model="dateRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" :default-time="defaultTime" /></el-form-item>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </el-form>
    <div class="toolbar"><el-button @click="exportData">导出</el-button></div>
    <el-table :data="rows" v-loading="loading" border>
      <el-table-column prop="createTime" label="时间" min-width="170" />
      <el-table-column prop="loginName" label="外管" />
      <el-table-column prop="regionId" label="大区" width="90"><template #default="{row}">{{ regionName(row.regionId) }}</template></el-table-column>
      <el-table-column prop="roleCode" label="角色" />
      <el-table-column prop="superAdminName" label="所属超级管理员" min-width="130" />
      <el-table-column prop="operationType" label="操作类型" min-width="130" />
      <el-table-column prop="target" label="对象" />
      <el-table-column prop="content" label="内容" min-width="220" />
    </el-table>
    <pagination v-show="total > 0" v-model:page="query.pageNum" v-model:limit="query.pageSize" :total="total" @pagination="load" />
  </div>
</template>

<script setup>
import { listExternalOperationLogs } from '@/api/external/manager'

const { proxy } = getCurrentInstance()
const types = [
  { value: 'BAN', label: '执行封禁' },
  { value: 'UNBAN', label: '解除封禁' },
  { value: 'CREATE_SUBORDINATE', label: '新增下级' },
  { value: 'CREATE_GUILD', label: '新建公会' },
  { value: 'ISSUE_GIFT', label: '赠送礼包' },
  { value: 'ASSIGN_CHAT_NO', label: '分配靓号' }
]
const regions = [{ value: 1, label: '全球' }, { value: 2, label: '东南亚' }, { value: 3, label: '华语' }, { value: 4, label: '中东' }, { value: 5, label: '印度' }, { value: 6, label: '越南' }, { value: 7, label: '菲律宾' }]
const regionName = value => regions.find(item => item.value === value)?.label || value || '--'
const defaultTime = [new Date(2000, 0, 1, 0, 0, 0), new Date(2000, 0, 1, 23, 59, 59)]
const dateRange = ref([])
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, loginName: '', superAdminName: '', operationType: '', regionId: undefined, beginTime: undefined, endTime: undefined })

function syncDateRange() {
  query.beginTime = dateRange.value?.[0]
  query.endTime = dateRange.value?.[1]
}

async function load() {
  syncDateRange()
  loading.value = true
  try {
    const res = await listExternalOperationLogs(query)
    rows.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  load()
}

function reset() {
  Object.assign(query, { pageNum: 1, loginName: '', superAdminName: '', operationType: '', regionId: undefined, beginTime: undefined, endTime: undefined })
  dateRange.value = []
  load()
}

function exportData() {
  syncDateRange()
  proxy.download('external/operation-logs/export', {
    loginName: query.loginName,
    superAdminName: query.superAdminName,
    operationType: query.operationType,
    regionId: query.regionId,
    beginTime: query.beginTime,
    endTime: query.endTime
  }, `外管操作记录_${Date.now()}.xlsx`, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded', externalToken: false }
  })
}

load()
</script>

<style scoped>
.toolbar { margin: 0 0 16px; text-align: right; }
</style>
