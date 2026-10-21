import request from '@/utils/request'

// 查询平台大盘聚合数据
export function getPlatformDashboard(params) {
  return request({
    url: '/bi/platform',
    method: 'get',
    params
  })
}

// 查询平台大盘筛选维度
export function getPlatformDimensions() {
  return request({
    url: '/bi/platform/dimensions',
    method: 'get'
  })
}
