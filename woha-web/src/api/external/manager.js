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
export const listExternalAccount = (params) => request({ url: '/external/accounts/list', method: 'get', params })
/** 查询外管账号详情 */
export const getExternalAccount = (id) => request({ url: `/external/accounts/${id}`, method: 'get' })
/** 新增外管账号 */
export const addExternalAccount = (data) => request({ url: '/external/accounts', method: 'post', data })
/** 修改外管账号 */
export const updateExternalAccount = (id, data) => request({ url: `/external/accounts/${id}`, method: 'put', data })
/** 修改账号状态 */
export const changeExternalAccountStatus = (id, status) => request({ url: `/external/accounts/${id}/status`, method: 'put', data: { status } })
/** 删除账号 */
export const delExternalAccount = (id) => request({ url: `/external/accounts/${id}`, method: 'delete' })
/** 重置账号密码 */
export const resetExternalAccountPassword = (id, password) => request({ url: `/external/accounts/${id}/password`, method: 'put', data: { password } })
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
