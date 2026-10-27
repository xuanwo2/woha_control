<template>
  <div class="external-login"><el-card class="login-card"><h2>Woha 外管后台</h2><el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="submit"><el-form-item prop="loginName"><el-input v-model="form.loginName" placeholder="登录账号" /></el-form-item><el-form-item prop="password"><el-input v-model="form.password" type="password" show-password placeholder="密码" /></el-form-item><el-button type="primary" :loading="loading" class="full" @click="submit">登录</el-button></el-form></el-card></div>
</template>

<script setup>
import { externalLogin } from '@/api/external/manager'
import { setExternalToken } from '@/utils/externalAuth'
const router = useRouter(); const formRef = ref(); const loading = ref(false); const form = reactive({ loginName: '', password: '' }); const rules = { loginName: [{ required: true, message: '请输入登录账号', trigger: 'blur' }], password: [{ required: true, message: '请输入密码', trigger: 'blur' }] }
function submit() { formRef.value.validate(async valid => { if (!valid) return; loading.value = true; try { const data = await externalLogin(form); setExternalToken(data.token); router.replace(data.needChangePassword ? '/external/password' : '/external/workbench') } finally { loading.value = false } }) }
</script>

<style scoped lang="scss">.external-login { min-height: 100%; display: flex; align-items: center; justify-content: center; background: #eef3f8; }.login-card { width: 380px; }.login-card h2 { text-align: center; margin-bottom: 28px; }.full { width: 100%; }</style>
