import request from '@/utils/request'

/**
 * 上传文档
 */
export function uploadDocument(formData) {
  return request({
    url: '/api/development/document/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

/**
 * 获取文档列表（分页）
 */
export function getDocumentList(params) {
  return request({
    url: '/api/development/document/list',
    method: 'get',
    params
  })
}

/**
 * 获取文档详情
 */
export function getDocumentById(id) {
  return request({
    url: `/api/development/document/${id}`,
    method: 'get'
  })
}

/**
 * 下载文档
 */
export function downloadDocument(id) {
  return request({
    url: `/api/development/document/download/${id}`,
    method: 'get'
  })
}

/**
 * 更新文档信息
 */
export function updateDocument(id, data) {
  return request({
    url: `/api/development/document/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除文档
 */
export function deleteDocument(id) {
  return request({
    url: `/api/development/document/${id}`,
    method: 'delete'
  })
}

/**
 * 创建新版本
 */
export function createDocumentVersion(id, version, uploadUserId) {
  return request({
    url: `/api/development/document/${id}/version`,
    method: 'post',
    params: { version, uploadUserId }
  })
}

/**
 * 按项目查询文档
 */
export function getDocumentsByProject(projectId) {
  return request({
    url: `/api/development/document/project/${projectId}`,
    method: 'get'
  })
}
