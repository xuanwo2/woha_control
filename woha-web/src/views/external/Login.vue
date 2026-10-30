<template>
  <div class="external-login" :dir="isRtl ? 'rtl' : 'ltr'"><el-select class="language-select" :model-value="locale" :aria-label="t('language')" @change="setLocale"><el-option v-for="item in EXTERNAL_LOCALES" :key="item.value" :label="item.label" :value="item.value" /></el-select><el-card class="login-card"><h2>{{ t('portalName') }}</h2><el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="submit"><el-form-item prop="loginName"><el-input v-model="form.loginName" name="username" autocomplete="username" :placeholder="t('account')" /></el-form-item><el-form-item prop="password"><el-input v-model="form.password" type="password" name="password" autocomplete="current-password" show-password :placeholder="t('password')" /></el-form-item><el-button type="primary" :loading="loading" class="full" @click="submit">{{ t('login') }}</el-button></el-form></el-card></div>
</template>

<script setup>
import { externalLogin } from '@/api/external/manager'
import { setExternalToken } from '@/utils/externalAuth'
import { EXTERNAL_LOCALES, useExternalI18n } from '@/i18n/external'
const router = useRouter(); const formRef = ref(); const loading = ref(false); const form = reactive({ loginName: '', password: '' });
const { locale, isRtl, setLocale, t } = useExternalI18n()
const rules = computed(() => ({ loginName: [{ required: true, message: t('enterAccount'), trigger: 'blur' }], password: [{ required: true, message: t('enterPassword'), trigger: 'blur' }] }))
function submit() { formRef.value.validate(async valid => { if (!valid) return; loading.value = true; try { const data = await externalLogin(form); setExternalToken(data.token); router.replace(data.needChangePassword ? '/external/password' : '/external/workbench') } finally { loading.value = false } }) }
</script>

<style scoped lang="scss">.external-login { min-height: 100%; display: flex; align-items: center; justify-content: center; background: #eef3f8; }.language-select { position:absolute; top:20px; right:28px; width:112px; }.login-card { width: 380px; }.login-card h2 { text-align: center; margin-bottom: 28px; }.full { width: 100%; }.external-login[dir='rtl'] .language-select { right:auto; left:28px; }</style>
