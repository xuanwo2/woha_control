import request from '@/utils/request'

// 查询玩法看板聚合数据
export function getPlayDashboard(params) {
  return request({
    url: '/bi/play',
    method: 'get',
    params
  })
}

// 查询玩法看板可选地区
export function getPlayDimensions() {
  return request({
    url: '/bi/play/dimensions',
    method: 'get'
  })
}
