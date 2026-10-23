<template>
  <div class="external-page"><el-form inline><el-form-item label="大区"><el-select v-model="regionId" @change="load"><el-option v-for="item in regions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-form><el-table :data="permissions" border><el-table-column prop="label" label="功能权限" /><el-table-column v-for="role in roles" :key="role.key" :label="role.label" align="center"><template #default="{ row }"><el-checkbox v-model="form[role.key]" :label="row.value"><span /></el-checkbox></template></el-table-column></el-table><el-button type="primary" style="margin-top:16px" @click="save">保存</el-button></div>
</template>
<script setup>
import { getExternalRolePermissions, saveExternalRolePermissions } from '@/api/external/manager'
const regions = [{ value: 1, label: '全球' }, { value: 2, label: '东南亚' }, { value: 3, label: '华语' }, { value: 4, label: '中东' }, { value: 5, label: '印度' }, { value: 6, label: '越南' }, { value: 7, label: '菲律宾' }]; const roles = [{ key: 'superAdminPermissions', label: 'Super Admin' }, { key: 'bdLeaderPermissions', label: 'BD Leader' }, { key: 'bdPermissions', label: 'BD' }]; const permissions = [{ value: 'VIEW_TEAM_GUILD', label: '查看团队与公会' }, { value: 'VIEW_GUILD_FLOW', label: '查看公会流水' }, { value: 'CREATE_SUBORDINATE', label: '新增下级' }, { value: 'CREATE_GUILD', label: '新建公会' }, { value: 'SEND_GIFT_PACK', label: '赠送礼包' }, { value: 'ASSIGN_CHAT_NO', label: '分配靓号' }, { value: 'EXECUTE_BAN', label: '执行封禁' }, { value: 'SUBMIT_REPORT', label: '提交举报' }, { value: 'EXPORT_GUILD_LIST', label: '导出公会列表' }, { value: 'EXPORT_GUILD_FLOW', label: '导出公会流水' }, { value: 'EXPORT_TEAM_MEMBER', label: '导出团队成员' }, { value: 'EXPORT_GIFT_ISSUE', label: '导出礼包赠送记录' }, { value: 'EXPORT_ACCOUNT_ASSIGNMENT', label: '导出账号分配记录' }]; const regionId = ref(2); const form = reactive({ superAdminPermissions: [], bdLeaderPermissions: [], bdPermissions: [] })
async function load() { const data = await getExternalRolePermissions(regionId.value); Object.assign(form, data.data) }; async function save() { await saveExternalRolePermissions({ regionId: regionId.value, ...form }) }; load()
</script>
<style scoped>
.external-page {
  box-sizing: border-box;
  padding: 20px;
}
</style>
