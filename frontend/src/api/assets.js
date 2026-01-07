import request from '@/utils/request'

export function getQuota(userId, year) {
  return request({
    url: '/api/assets/quota/get',
    method: 'get',
    params: { userId, year }
  })
}

export function listQuotas(userId) {
  return request({
    url: '/api/assets/quota/list',
    method: 'get',
    params: { userId }
  })
}

export function getCreditList(userId) {
  return request({
    url: '/api/assets/credit/list',
    method: 'get',
    params: { userId }
  })
}

export function getQuotaDetails(quotaId) {
  return request({
    url: '/api/assets/quota/detail/list',
    method: 'get',
    params: { quotaId }
  })
}
