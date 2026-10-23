<template>
  <div class="external-page">
    <div class="summary"><el-card><div class="number">{{ availableCount }}</div><div>可用靓号</div></el-card><el-card><div class="number">{{ assignedCount }}</div><div>已分配靓号</div></el-card></div>
    <el-card><template #header>分配靓号</template><el-form inline :model="assignForm"><el-form-item label="靓号"><el-select v-model="assignForm.chatNo" filterable placeholder="选择可用靓号"><el-option v-for="item in availableItems" :key="item.chatNo" :label="item.chatNo" :value="item.chatNo"/></el-select></el-form-item><el-form-item label="用户Woha ID"><el-input v-model.number="assignForm.receiverChatNo"/></el-form-item><el-form-item label="有效期"><el-select v-model="assignForm.dayNum"><el-option label="30天" :value="30"/><el-option label="90天" :value="90"/><el-option label="180天" :value="180"/></el-select></el-form-item><el-button type="primary" @click="assign">确认分配</el-button></el-form></el-card>
    <el-card style="margin-top:16px"><template #header>我的靓号池</template><el-table :data="items" v-loading="loading" border><el-table-column prop="chatNo" label="靓号"/><el-table-column label="状态"><template #default="{row}"><el-tag :type="row.status === '0' ? 'success' : 'warning'">{{ row.status === '0' ? '可用' : '已分配' }}</el-tag></template></el-table-column><el-table-column prop="receiverUid" label="接收用户UID"/><el-table-column prop="expireTime" label="到期时间" min-width="170"/></el-table></el-card>
  </div>
</template>
<script setup>
import { ElMessage } from 'element-plus'
import { assignExternalChatNo, listExternalChatNos } from '@/api/external/manager'
const loading = ref(false); const items = ref([]); const assignForm = reactive({ chatNo: undefined, receiverChatNo: undefined, dayNum: 30 })
const availableItems = computed(() => items.value.filter(item => item.status === '0')); const availableCount = computed(() => availableItems.value.length); const assignedCount = computed(() => items.value.filter(item => item.status === '1').length)
async function load() { loading.value = true; try { const response = await listExternalChatNos(); items.value = response.data } finally { loading.value = false } }
async function assign() { if (!assignForm.chatNo || !assignForm.receiverChatNo) return ElMessage.warning('请选择靓号并填写用户Woha ID'); await assignExternalChatNo(assignForm); ElMessage.success('分配成功'); assignForm.chatNo = undefined; assignForm.receiverChatNo = undefined; await load() }
load()
</script>
<style scoped>.external-page{padding:20px}.summary{display:flex;gap:16px;margin-bottom:16px}.summary .el-card{width:180px}.number{font-size:28px;font-weight:600;margin-bottom:8px}</style>
