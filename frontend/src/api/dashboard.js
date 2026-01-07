import request from '../utils/request'

/**
 * 获取仪表盘汇总数据
 */
export function getDashboardSummary(userId) {
  return request({
    url: '/api/dashboard/summary',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取用户个人统计数据
 */
export function getUserStats(userId) {
  return request({
    url: '/api/dashboard/user-stats',
    method: 'get',
    params: { userId }
  })
}
