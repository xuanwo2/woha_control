<template>
  <div class="password-page" :dir="isRtl ? 'rtl' : 'ltr'">
    <el-select class="language-select" :model-value="locale" :aria-label="t('language')" @change="setLocale"><el-option v-for="item in EXTERNAL_LOCALES" :key="item.value" :label="item.label" :value="item.value" /></el-select>
    <el-card>
      <h3>{{ t('setNewPassword') }}</h3>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" autocomplete="off">
        <el-form-item :label="t('oldPassword')" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" name="current-password" autocomplete="off" :readonly="!oldPasswordEditable" autocapitalize="off" :spellcheck="false" show-password @focus="oldPasswordEditable = true" />
        </el-form-item>
        <el-form-item :label="t('newPassword')" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" name="new-password" autocomplete="off" :readonly="!newPasswordEditable" autocapitalize="off" :spellcheck="false" show-password @focus="newPasswordEditable = true" />
        </el-form-item>
        <el-form-item><el-button type="primary" @click="submit">{{ t('confirmAndEnter') }}</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { changeExternalPassword } from '@/api/external/manager'
import { EXTERNAL_LOCALES, useExternalI18n } from '@/i18n/external'
import { removeExternalToken } from '@/utils/externalAuth'
const router = useRouter(); const formRef = ref(); const form = reactive({ oldPassword: '', newPassword: '' });
// 聚焦前保持只读，避免 Safari 在页面加载时自动生成并填充强密码。
const oldPasswordEditable = ref(false)
const newPasswordEditable = ref(false)
const { locale, isRtl, setLocale, t } = useExternalI18n()
function validateNewPassword(rule, value, callback) {
  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,50}$/.test(value)) return callback(new Error(t('passwordRule')))
  if (value === form.oldPassword) return callback(new Error(t('passwordCannotMatchOld')))
  callback()
}
const rules = computed(() => ({ oldPassword: [{ required: true, message: t('enterPassword'), trigger: 'blur' }], newPassword: [{ required: true, validator: validateNewPassword, trigger: 'blur' }] }))
function submit() { formRef.value.validate(async valid => { if (!valid) return; await changeExternalPassword(form); removeExternalToken(); router.replace('/external/login') }) }
</script>
<style scoped>.password-page { position:relative; max-width:480px; margin:100px auto; }.language-select { position:fixed; top:20px; right:28px; width:112px; }.password-page[dir='rtl'] .language-select { right:auto; left:28px; }</style>
