<template>
  <div class="external-layout">
    <header><strong>Woha 外管后台</strong><span class="hint">外管使用，独立网址与账号，与 Woha 管理后台分开</span><span class="profile">{{ profile.realName || profile.loginName }} · {{ roleName }}</span><el-button link @click="logout">退出</el-button></header>
    <aside><RouterLink to="/external-workbench">工作台</RouterLink><RouterLink to="/external/team">团队管理</RouterLink><RouterLink to="/external/guild">公会管理</RouterLink><RouterLink to="/external/guild-flow">公会流水</RouterLink><RouterLink to="/external/gift">礼包赠送</RouterLink><RouterLink to="/external/chat-no">靓号</RouterLink><RouterLink to="/external/account-ban">账号封禁</RouterLink><RouterLink to="/external/export">数据导出</RouterLink></aside>
    <main><RouterView /></main>
  </div>
</template>

<script setup>
import { externalLogout, getExternalProfile } from '@/api/external/manager'
import { removeExternalToken } from '@/utils/externalAuth'
const router = useRouter()
const profile = ref({})
getExternalProfile().then(data => { profile.value = data.data })
const roleName = computed(() => ({ SUPER_ADMIN: 'Super Admin', BD_LEADER: 'BD Leader', BD: 'BD' })[profile.value.roleCode] || '')
function logout() { externalLogout().finally(() => { removeExternalToken(); router.replace('/external/login') }) }
</script>

<style scoped lang="scss">
.external-layout { min-height: 100%; background: #f5f7fa; display:grid; grid-template-columns:220px 1fr; grid-template-rows:56px 1fr; } header { grid-column:1 / 3; height:56px; display:flex; align-items:center; gap:20px; padding:0 28px; background:#fff; border-bottom:1px solid #dcdfe6; } .hint { color:#909399; font-size:13px; } .profile { margin-left:auto; color:#606266; } aside { background:#fff; border-right:1px solid #dcdfe6; padding:12px 0; } aside a { display:block; padding:13px 28px; color:#606266; text-decoration:none; } aside a.router-link-active { color:#409eff; background:#ecf5ff; border-right:3px solid #409eff; } main { min-width:0; } @media(max-width:768px){.external-layout{grid-template-columns:1fr;grid-template-rows:auto auto 1fr}header{grid-column:auto;flex-wrap:wrap;height:auto;padding:12px}.hint{display:none}.profile{margin-left:auto}aside{display:flex;overflow:auto;padding:0}aside a{white-space:nowrap;padding:12px}main{overflow:auto}}
</style>
