import request from '@/utils/request'

// 查询营收看板聚合数据
export function getRevenueDashboard(params) {
  return request({
    url: '/bi/revenue',
    method: 'get',
    params
  })
}

// 查询营收看板可选地区
export function getRevenueDimensions() {
  return request({
    url: '/bi/revenue/dimensions',
    method: 'get'
  })
}
