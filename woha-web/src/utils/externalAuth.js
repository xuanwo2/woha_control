const EXTERNAL_TOKEN_KEY = 'external_manager_token'

/** 获取外管登录令牌 */
export function getExternalToken() {
  return localStorage.getItem(EXTERNAL_TOKEN_KEY)
}

/** 保存外管登录令牌 */
export function setExternalToken(token) {
  localStorage.setItem(EXTERNAL_TOKEN_KEY, token)
}

/** 删除外管登录令牌 */
export function removeExternalToken() {
  localStorage.removeItem(EXTERNAL_TOKEN_KEY)
}
