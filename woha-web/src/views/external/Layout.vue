<template>
  <div class="external-layout">
    <header><strong>Woha 外管后台</strong><nav><RouterLink to="/external/accounts">外管账号</RouterLink><RouterLink to="/external/role-permissions">角色默认权限</RouterLink></nav><span>{{ profile.realName || profile.loginName }}</span><el-button link @click="logout">退出</el-button></header>
    <main><RouterView /></main>
  </div>
</template>

<script setup>
import { externalLogout, getExternalProfile } from '@/api/external/manager'
import { removeExternalToken } from '@/utils/externalAuth'
const router = useRouter()
const profile = ref({})
getExternalProfile().then(data => { profile.value = data.data })
function logout() { externalLogout().finally(() => { removeExternalToken(); router.replace('/external/login') }) }
</script>

<style scoped lang="scss">
.external-layout { min-height: 100%; background: #f5f7fa; } header { height: 56px; display: flex; align-items: center; gap: 28px; padding: 0 28px; background: #fff; border-bottom: 1px solid #dcdfe6; } nav { display: flex; gap: 20px; flex: 1; } nav a { color: #606266; text-decoration: none; } nav a.router-link-active { color: #409eff; } main { padding: 20px 28px; }
</style>
