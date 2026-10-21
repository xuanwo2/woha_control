import request from '@/utils/request'

// 查询用户增长看板聚合数据
export function getUserGrowthDashboard(params) {
  return request({
    url: '/bi/user-growth',
    method: 'get',
    params
  })
}

// 查询用户增长看板筛选维度
export function getUserGrowthDimensions() {
  return request({
    url: '/bi/user-growth/dimensions',
    method: 'get'
  })
}
