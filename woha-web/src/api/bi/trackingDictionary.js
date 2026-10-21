import request from '@/utils/request'

// 查询埋点事件字典列表
export function listTrackingEvents(params) {
  return request({
    url: '/bi/tracking-dictionary/event/list',
    method: 'get',
    params
  })
}

// 查询埋点事件详情
export function getTrackingEvent(eventId) {
  return request({
    url: `/bi/tracking-dictionary/event/${eventId}`,
    method: 'get'
  })
}

// 查询埋点业务模块选项
export function getTrackingModules() {
  return request({
    url: '/bi/tracking-dictionary/event/modules',
    method: 'get'
  })
}

// 登记新埋点事件
export function addTrackingEvent(data) {
  return request({
    url: '/bi/tracking-dictionary/event',
    method: 'post',
    data
  })
}

// 提交埋点事件修改审批
export function submitTrackingEventChange(data) {
  return request({
    url: '/bi/tracking-dictionary/event/submit',
    method: 'put',
    data
  })
}

// 更新埋点事件状态
export function updateTrackingEventStatus(eventId, status) {
  return request({
    url: `/bi/tracking-dictionary/event/${eventId}/status`,
    method: 'put',
    params: { status }
  })
}

// 查询埋点公共参数列表
export function listTrackingCommonParams(params) {
  return request({
    url: '/bi/tracking-dictionary/common-param/list',
    method: 'get',
    params
  })
}

// 新增埋点公共参数
export function addTrackingCommonParam(data) {
  return request({
    url: '/bi/tracking-dictionary/common-param',
    method: 'post',
    data
  })
}

// 更新埋点公共参数
export function updateTrackingCommonParam(data) {
  return request({
    url: '/bi/tracking-dictionary/common-param',
    method: 'put',
    data
  })
}

// 更新埋点公共参数状态
export function updateTrackingCommonParamStatus(commonParamId, status) {
  return request({
    url: `/bi/tracking-dictionary/common-param/${commonParamId}/status`,
    method: 'put',
    params: { status }
  })
}

// 查询埋点变更审批列表
export function listTrackingChanges(params) {
  return request({
    url: '/bi/tracking-dictionary/change/list',
    method: 'get',
    params
  })
}

// 审批埋点事件变更
export function reviewTrackingChange(changeId, data) {
  return request({
    url: `/bi/tracking-dictionary/change/${changeId}/review`,
    method: 'put',
    data
  })
}
