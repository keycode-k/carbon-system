import request from '@/utils/request'

/**
 * 根据用户ID获取企业信息
 */
export function getCompanyInfo(userId) {
  return request({
    url: `/api/company/info/${userId}`,
    method: 'get'
  })
}

/**
 * 提交企业认证
 */
export function submitCertification(data) {
  return request({
    url: '/api/company/submit',
    method: 'post',
    data
  })
}

/**
 * 保存草稿
 */
export function saveDraft(data) {
  return request({
    url: '/api/company/draft',
    method: 'post',
    data
  })
}

/**
 * 更新企业信息
 */
export function updateCompany(id, data) {
  return request({
    url: `/api/company/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除企业信息
 */
export function deleteCompany(id) {
  return request({
    url: `/api/company/${id}`,
    method: 'delete'
  })
}

/**
 * 分页查询企业列表（管理员）
 */
export function getCompanyList(params) {
  return request({
    url: '/api/company/list',
    method: 'get',
    params
  })
}

/**
 * 审核企业认证（管理员）
 */
export function reviewCertification(data) {
  return request({
    url: '/api/company/review',
    method: 'post',
    params: data
  })
}

/**
 * 获取待审核企业数量（管理员）
 */
export function getPendingCount() {
  return request({
    url: '/api/company/pending/count',
    method: 'get'
  })
}
