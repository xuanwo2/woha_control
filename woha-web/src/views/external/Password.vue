<template>
  <div class="password-page"><el-card><h3>设置新密码</h3><el-form ref="formRef" :model="form" :rules="rules" label-width="90px"><el-form-item label="原密码" prop="oldPassword"><el-input v-model="form.oldPassword" type="password" show-password /></el-form-item><el-form-item label="新密码" prop="newPassword"><el-input v-model="form.newPassword" type="password" show-password /></el-form-item><el-form-item><el-button type="primary" @click="submit">确认并进入</el-button></el-form-item></el-form></el-card></div>
</template>
<script setup>
import { changeExternalPassword } from '@/api/external/manager'
const router = useRouter(); const formRef = ref(); const form = reactive({ oldPassword: '', newPassword: '' }); const rules = { oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }], newPassword: [{ required: true, pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,50}$/, message: '至少 8 位，且包含字母和数字', trigger: 'blur' }] }
function submit() { formRef.value.validate(async valid => { if (!valid) return; await changeExternalPassword(form); router.replace('/external/workbench') }) }
</script>
<style scoped>.password-page { max-width: 480px; margin: 100px auto; }</style>
