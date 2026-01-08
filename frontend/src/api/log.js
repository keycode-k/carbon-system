import request from '@/utils/request'

/**
 * 获取操作日志列表
 * @param {Object} params 查询参数
 */
export function getOperationLogList(params) {
  return request({
    url: '/api/system/log/list',
    method: 'get',
    params
  })
}

/**
 * 获取操作日志详情
 * @param {Number} id 日志ID
 */
export function getOperationLogDetail(id) {
  return request({
    url: `/api/system/log/${id}`,
    method: 'get'
  })
}

/**
 * 清理日志
 * @param {Number} days 保留天数
 */
export function cleanOperationLog(days) {
  return request({
    url: '/api/system/log/clean',
    method: 'delete',
    params: { days }
  })
}
