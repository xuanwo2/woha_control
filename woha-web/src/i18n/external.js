import { computed, ref } from 'vue'

/** 外管后台支持的语言。 */
export const EXTERNAL_LOCALES = [
  { value: 'zh-CN', label: '中文' },
  { value: 'en', label: 'English' },
  { value: 'ar', label: 'العربية' }
]

const storageKey = 'woha-external-locale'
const supportedLocales = EXTERNAL_LOCALES.map(item => item.value)
const savedLocale = localStorage.getItem(storageKey)

/** 当前界面语言，未选择时默认使用中文。 */
export const externalLocale = ref(supportedLocales.includes(savedLocale) ? savedLocale : 'zh-CN')

const messages = {
  'zh-CN': {
    portalName: 'Woha 外管后台', account: '登录账号', password: '密码', login: '登录', logout: '退出',
    oldPassword: '原密码', newPassword: '新密码', setNewPassword: '设置新密码', confirmAndEnter: '确认并进入',
    enterAccount: '请输入登录账号', enterPassword: '请输入密码', passwordRule: '至少 8 位，且包含字母和数字', passwordCannotMatchOld: '新密码不能与原密码相同',
    portalHint: '外管使用，独立网址与账号，与 Woha 管理后台分开',
    dashboard: '工作台', team: '团队管理', agencies: '公会管理', agencyRevenue: '公会流水',
    giftPacks: '礼包赠送', specialIds: '靓号', bans: '账号封禁', export: '数据导出',
    changePassword: '修改密码', language: '语言', roleSuperAdmin: 'Super Admin', roleBdLeader: 'BD Leader', roleBd: 'BD',
    agenciesCount: '公会', anchors: '主播', todayTeamRevenue: '今日团队流水（金币）', agencyRevenueGold: '公会流水（金币）',
    daily: '日流水', weekly: '周流水', monthly: '月流水', total: '总流水', quotaAndAuthorization: '额度与授权余量',
    noQuota: '当前账号暂无可展示的额度或授权。', recentOperations: '最近操作（本人）', time: '时间', operation: '操作', target: '对象', content: '内容', noOperations: '暂无操作记录。', remaining: '剩余', available: '可用', usedToday: '今日已用'
  },
  en: {
    portalName: 'Woha BD Portal', account: 'Account', password: 'Password', login: 'Log in', logout: 'Log out',
    oldPassword: 'Current password', newPassword: 'New password', setNewPassword: 'Set a new password', confirmAndEnter: 'Confirm and enter',
    enterAccount: 'Enter your account', enterPassword: 'Enter your password', passwordRule: 'At least 8 characters with letters and numbers', passwordCannotMatchOld: 'The new password must differ from the current password',
    portalHint: 'A separate portal and account for BD operations.',
    dashboard: 'Dashboard', team: 'Team', agencies: 'Agencies', agencyRevenue: 'Agency revenue',
    giftPacks: 'Gift packs', specialIds: 'Special IDs', bans: 'Bans', export: 'Export',
    changePassword: 'Change password', language: 'Language', roleSuperAdmin: 'Super Admin', roleBdLeader: 'BD Leader', roleBd: 'BD',
    agenciesCount: 'Agencies', anchors: 'Anchors', todayTeamRevenue: "Today's team revenue (coins)", agencyRevenueGold: 'Agency revenue (coins)',
    daily: 'Daily', weekly: 'Weekly', monthly: 'Monthly', total: 'Total', quotaAndAuthorization: 'Quota and authorization balance',
    noQuota: 'No quota or authorization is available for this account.', recentOperations: 'Recent operations', time: 'Time', operation: 'Operation', target: 'Target', content: 'Content', noOperations: 'No operation records.', remaining: 'Remaining', available: 'Available', usedToday: 'Used today'
  },
  ar: {
    portalName: 'بوابة Woha للوكلاء', account: 'اسم الحساب', password: 'كلمة المرور', login: 'تسجيل الدخول', logout: 'تسجيل الخروج',
    oldPassword: 'كلمة المرور الحالية', newPassword: 'كلمة المرور الجديدة', setNewPassword: 'تعيين كلمة مرور جديدة', confirmAndEnter: 'تأكيد والدخول',
    enterAccount: 'أدخل اسم الحساب', enterPassword: 'أدخل كلمة المرور', passwordRule: 'ثمانية أحرف على الأقل، تتضمن حروفاً وأرقاماً', passwordCannotMatchOld: 'يجب أن تختلف كلمة المرور الجديدة عن كلمة المرور الحالية',
    portalHint: 'بوابة وحساب منفصلان لعمليات الوكلاء.',
    dashboard: 'لوحة التحكم', team: 'الفريق', agencies: 'الوكالات', agencyRevenue: 'إيرادات الوكالات',
    giftPacks: 'حزم الهدايا', specialIds: 'الأرقام المميزة', bans: 'الحظر', export: 'تصدير البيانات',
    changePassword: 'تغيير كلمة المرور', language: 'اللغة', roleSuperAdmin: 'Super Admin', roleBdLeader: 'BD Leader', roleBd: 'BD',
    agenciesCount: 'الوكالات', anchors: 'المضيفون', todayTeamRevenue: 'إيرادات الفريق اليوم (عملات)', agencyRevenueGold: 'إيرادات الوكالات (عملات)',
    daily: 'يومي', weekly: 'أسبوعي', monthly: 'شهري', total: 'الإجمالي', quotaAndAuthorization: 'الرصيد المتاح والصلاحيات',
    noQuota: 'لا توجد حصة أو صلاحيات متاحة لهذا الحساب.', recentOperations: 'العمليات الأخيرة', time: 'الوقت', operation: 'العملية', target: 'الهدف', content: 'المحتوى', noOperations: 'لا توجد سجلات عمليات.', remaining: 'المتبقي', available: 'متاح', usedToday: 'المستخدم اليوم'
  }
}

/** 切换语言并持久化用户选择。 */
export function setExternalLocale(locale) {
  if (!supportedLocales.includes(locale)) return
  externalLocale.value = locale
  localStorage.setItem(storageKey, locale)
}

/** 提供外管后台的三语文案读取能力。 */
export function useExternalI18n() {
  const isRtl = computed(() => externalLocale.value === 'ar')
  const t = key => messages[externalLocale.value]?.[key] || messages['zh-CN'][key] || key
  return { locale: externalLocale, isRtl, setLocale: setExternalLocale, t }
}
