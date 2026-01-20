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

export function createCredit(creditData) {
  return request({
    url: '/api/assets/credit/create',
    method: 'post',
    data: creditData
  })
}

export function listCredit(id) {
  return request({
    url: '/api/assets/credit/list',
    method: 'post',
    params: { id }
  })
}

export function retireCredit(id) {
  return request({
    url: '/api/assets/credit/retire',
    method: 'post',
    params: { id }
  })
}
