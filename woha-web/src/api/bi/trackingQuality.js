import request from '@/utils/request'

// 查询埋点质量监控看板
export function getTrackingQualityDashboard() {
  return request({
    url: '/bi/tracking-quality',
    method: 'get'
  })
}

// 刷新埋点质量检测结果
export function refreshTrackingQuality() {
  return request({
    url: '/bi/tracking-quality/refresh',
    method: 'post'
  })
}

// 查询埋点质量工单列表
export function listTrackingQualityIssues(params) {
  return request({
    url: '/bi/tracking-quality/issue/list',
    method: 'get',
    params
  })
}

// 更新埋点质量工单处理结果
export function updateTrackingQualityIssue(issueId, data) {
  return request({
    url: `/bi/tracking-quality/issue/${issueId}`,
    method: 'put',
    data
  })
}
