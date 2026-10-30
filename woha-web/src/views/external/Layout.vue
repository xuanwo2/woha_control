<template>
  <div class="external-layout" :dir="isRtl ? 'rtl' : 'ltr'">
    <header><strong>{{ t('portalName') }}</strong><span class="hint">{{ t('portalHint') }}</span><span class="profile">{{ profile.realName || profile.loginName }} · {{ roleName }}</span><el-select class="language-select" :model-value="locale" :aria-label="t('language')" @change="setLocale"><el-option v-for="item in EXTERNAL_LOCALES" :key="item.value" :label="item.label" :value="item.value" /></el-select><el-button link @click="logout">{{ t('logout') }}</el-button></header>
    <aside><RouterLink to="/external/workbench">{{ t('dashboard') }}</RouterLink><RouterLink v-if="canViewTeamGuild" to="/external/team">{{ t('team') }}</RouterLink><RouterLink v-if="canViewTeamGuild" to="/external/guild">{{ t('agencies') }}</RouterLink><RouterLink v-if="canViewGuildFlow" to="/external/guild-flow">{{ t('agencyRevenue') }}</RouterLink><RouterLink v-if="canSendGiftPack" to="/external/gift">{{ t('giftPacks') }}</RouterLink><RouterLink v-if="canAssignChatNo" to="/external/chat-no">{{ t('specialIds') }}</RouterLink><RouterLink v-if="canExecuteBan" to="/external/account-ban">{{ t('bans') }}</RouterLink><RouterLink v-if="canExport" to="/external/export">{{ t('export') }}</RouterLink></aside>
    <main><RouterView /></main>
  </div>
</template>

<script setup>
import { externalLogout, getExternalProfile } from '@/api/external/manager'
import { removeExternalToken } from '@/utils/externalAuth'
import { EXTERNAL_LOCALES, useExternalI18n } from '@/i18n/external'
const router = useRouter()
const { locale, isRtl, setLocale, t } = useExternalI18n()
const profile = ref({})
getExternalProfile().then(data => { profile.value = data.data })
const roleName = computed(() => ({ SUPER_ADMIN: t('roleSuperAdmin'), BD_LEADER: t('roleBdLeader'), BD: t('roleBd') })[profile.value.roleCode] || '')
const permissions = computed(() => String(profile.value.permissions || '').split(','))
const canViewTeamGuild = computed(() => permissions.value.includes('VIEW_TEAM_GUILD'))
const canViewGuildFlow = computed(() => permissions.value.includes('VIEW_GUILD_FLOW'))
const canSendGiftPack = computed(() => permissions.value.includes('SEND_GIFT_PACK'))
const canAssignChatNo = computed(() => permissions.value.includes('ASSIGN_CHAT_NO'))
const canExecuteBan = computed(() => permissions.value.includes('EXECUTE_BAN'))
const canExport = computed(() => permissions.value.some(item => item.startsWith('EXPORT_')))
function logout() { externalLogout().finally(() => { removeExternalToken(); router.replace('/external/login') }) }
</script>

<style scoped lang="scss">
.external-layout { min-height: 100%; background: #f5f7fa; display:grid; grid-template-columns:220px 1fr; grid-template-rows:56px 1fr; } header { grid-column:1 / 3; height:56px; display:flex; align-items:center; gap:20px; padding:0 28px; background:#fff; border-bottom:1px solid #dcdfe6; } .hint { color:#909399; font-size:13px; } .profile { margin-left:auto; color:#606266; } .language-select { width:112px; } aside { background:#fff; border-right:1px solid #dcdfe6; padding:12px 0; } aside a { display:block; padding:13px 28px; color:#606266; text-decoration:none; } aside a.router-link-active { color:#409eff; background:#ecf5ff; border-right:3px solid #409eff; } main { min-width:0; } .external-layout[dir='rtl'] { grid-template-columns:1fr 220px; } .external-layout[dir='rtl'] header { grid-column:1 / 3; } .external-layout[dir='rtl'] aside { grid-column:2; grid-row:2; border-right:0; border-left:1px solid #dcdfe6; } .external-layout[dir='rtl'] main { grid-column:1; grid-row:2; } .external-layout[dir='rtl'] aside a.router-link-active { border-right:0; border-left:3px solid #409eff; } @media(max-width:768px){.external-layout,.external-layout[dir='rtl']{grid-template-columns:1fr;grid-template-rows:auto auto 1fr}.external-layout header,.external-layout[dir='rtl'] header{grid-column:auto;flex-wrap:wrap;height:auto;padding:12px}.hint{display:none}.profile{margin-left:auto}.external-layout aside,.external-layout[dir='rtl'] aside{grid-column:auto;grid-row:auto;display:flex;overflow:auto;padding:0;border-left:0}.external-layout main,.external-layout[dir='rtl'] main{grid-column:auto;grid-row:auto;overflow:auto}aside a{white-space:nowrap;padding:12px} }
</style>
