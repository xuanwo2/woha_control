import request from '@/utils/request'

// 查询邀请增长看板聚合数据
export function getInviteGrowthDashboard(params) {
  return request({
    url: '/bi/invite-growth',
    method: 'get',
    params
  })
}

// 查询邀请增长看板筛选维度
export function getInviteGrowthDimensions() {
  return request({
    url: '/bi/invite-growth/dimensions',
    method: 'get'
  })
}

// 查询邀请人质量分层列表
export function getInviteQualityList(params) {
  return request({
    url: '/bi/invite-growth/quality/list',
    method: 'get',
    params
  })
}
