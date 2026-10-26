import request from '@/utils/request'

/** 外管账号登录 */
export const externalLogin = (data) => request({ url: '/external/auth/login', method: 'post', data, headers: { isToken: false } })
/** 外管账号退出 */
export const externalLogout = () => request({ url: '/external/auth/logout', method: 'post' })
/** 查询当前外管账号 */
export const getExternalProfile = () => request({ url: '/external/auth/profile', method: 'get' })
/** 修改当前外管密码 */
export const changeExternalPassword = (data) => request({ url: '/external/auth/password', method: 'put', data })
/** 查询外管账号 */
export const listExternalAccount = (params) => request({ url: '/external/accounts/list', method: 'get', params, headers: { externalToken: false } })
/** 查询外管账号详情 */
export const getExternalAccount = (id) => request({ url: `/external/accounts/${id}`, method: 'get', headers: { externalToken: false } })
/** 新增外管账号 */
export const addExternalAccount = (data) => request({ url: '/external/accounts', method: 'post', data, headers: { externalToken: false } })
/** 修改外管账号 */
export const updateExternalAccount = (id, data) => request({ url: `/external/accounts/${id}`, method: 'put', data, headers: { externalToken: false } })
/** 修改账号状态 */
export const changeExternalAccountStatus = (id, status) => request({ url: `/external/accounts/${id}/status`, method: 'put', data: { status }, headers: { externalToken: false } })
/** 删除账号 */
export const delExternalAccount = (id) => request({ url: `/external/accounts/${id}`, method: 'delete', headers: { externalToken: false } })
/** 重置账号密码 */
export const resetExternalAccountPassword = (id, password) => request({ url: `/external/accounts/${id}/password`, method: 'put', data: { password }, headers: { externalToken: false } })
/** 查询角色默认权限 */
export const getExternalRolePermissions = (regionId) => request({ url: '/external/role-permissions', method: 'get', params: { regionId } })
/** 保存角色默认权限 */
export const saveExternalRolePermissions = (data) => request({ url: '/external/role-permissions', method: 'put', data })
/** 查询外管工作台 */
export const getExternalWorkbench = () => request({ url: '/external/workbench', method: 'get' })
/** 查询当前外管团队成员 */
export const listExternalTeamMembers = (params) => request({ url: '/external/team/members', method: 'get', params })
/** 新增下级 */
export const addExternalSubordinate = (data) => request({ url: '/external/team/subordinates', method: 'post', data })
/** 查询可选公会负责人 */
export const listExternalGuildResponsibles = () => request({ url: '/external/guild/responsibles', method: 'get' })
/** 新建公会 */
export const createExternalGuild = (data) => request({ url: '/external/guild', method: 'post', data })
/** 查询公会列表 */
export const listExternalGuild = (params) => request({ url: '/external/guild/list', method: 'get', params })
/** 查询公会流水汇总 */
export const getExternalGuildFlowSummary = (params) => request({ url: '/external/guild/flow-summary', method: 'get', params })
/** 查询外管礼包 */
export const listExternalGiftPacks = (params) => request({ url: '/external/gift-packs', method: 'get', params })
/** 保存外管礼包 */
export const saveExternalGiftPack = (data) => request({ url: '/external/gift-packs', method: 'post', data, headers: { externalToken: false } })
/** 查询外管礼包额度 */
export const listExternalGiftPackQuotas = (params) => request({ url: '/external/gift-packs/quotas', method: 'get', params })
/** 保存外管礼包额度 */
export const saveExternalGiftPackQuota = (data) => request({ url: '/external/gift-packs/quotas', method: 'put', data, headers: { externalToken: false } })
/** 发放外管礼包 */
export const issueExternalGiftPack = (data) => request({ url: '/external/gift-packs/issue', method: 'post', data })
/** 查询礼包发放记录 */
export const listExternalGiftPackIssues = () => request({ url: '/external/gift-packs/issues', method: 'get' })
/** 查询当前外管账号靓号池 */
export const listExternalChatNos = () => request({ url: '/external/chat-nos', method: 'get' })
/** 分配靓号 */
export const assignExternalChatNo = (data) => request({ url: '/external/chat-nos/assign', method: 'post', data })
/** 查询账号靓号池 */
export const listExternalAccountChatNos = (accountId) => request({ url: `/external/accounts/${accountId}/chat-nos`, method: 'get', headers: { externalToken: false } })
/** 向账号划拨靓号 */
export const addExternalAccountChatNo = (data) => request({ url: '/external/accounts/chat-nos', method: 'post', data, headers: { externalToken: false } })
/** 回收账号未分配靓号 */
export const delExternalAccountChatNo = (accountId, chatNo) => request({ url: `/external/accounts/${accountId}/chat-nos/${chatNo}`, method: 'delete', headers: { externalToken: false } })
/** 封禁账号 */
export const banExternalAccount = (data) => request({ url: '/external/account-bans', method: 'post', data })
/** 解除账号封禁 */
export const releaseExternalAccountBan = (chatNo) => request({ url: `/external/account-bans/${chatNo}/release`, method: 'post' })
/** 查询当前外管封禁处理记录 */
export const listExternalAccountBanRecords = () => request({ url: '/external/account-bans/records', method: 'get' })
/** 查询外管操作记录 */
export const listExternalOperationLogs = (params) => request({ url: '/external/operation-logs', method: 'get', params, headers: { externalToken: false } })
/** 导出公会列表 */
export const exportExternalGuildList = (data) => request({ url: '/external/guild/list/export', method: 'post', data, responseType: 'blob' })
/** 导出公会流水 */
export const exportExternalGuildFlow = (data) => request({ url: '/external/guild/export', method: 'post', data, responseType: 'blob' })
/** 导出团队成员 */
export const exportExternalTeam = () => request({ url: '/external/team/members/export', method: 'post', responseType: 'blob' })
/** 导出礼包赠送记录 */
export const exportExternalGiftIssues = () => request({ url: '/external/gift-packs/issues/export', method: 'post', responseType: 'blob' })
/** 导出账号分配记录 */
export const exportExternalAssignments = () => request({ url: '/external/account-assignments/export', method: 'post', responseType: 'blob' })
