import request from '@/utils/request'

export function getQuota(year) {
  return request({
    url: '/api/assets/quota/get',
    method: 'get',
    params: { year }
  })
}

export function listQuotas() {
  return request({
    url: '/api/assets/quota/list',
    method: 'get'
  })
}

export function getCreditList(token) {
  return request({
    url: '/api/assets/credit/list',
    method: 'get',
    params: { token }
  })
}
