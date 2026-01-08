import request from '@/utils/request'

/**
 * 分页查询方法学列表
 */
export function getMethodologyList(params) {
  return request({
    url: '/api/development/methodology/list',
    method: 'get',
    params
  })
}

/**
 * 查询所有有效方法学（用于下拉选择）
 */
export function getActiveMethodologies() {
  return request({
    url: '/api/development/methodology/active',
    method: 'get'
  })
}

/**
 * 根据ID查询方法学详情
 */
export function getMethodologyById(id) {
  return request({
    url: `/api/development/methodology/${id}`,
    method: 'get'
  })
}

/**
 * 新增方法学
 */
export function createMethodology(data) {
  return request({
    url: '/api/development/methodology',
    method: 'post',
    data
  })
}

/**
 * 更新方法学
 */
export function updateMethodology(id, data) {
  return request({
    url: `/api/development/methodology/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除方法学
 */
export function deleteMethodology(id) {
  return request({
    url: `/api/development/methodology/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除方法学
 */
export function batchDeleteMethodology(ids) {
  return request({
    url: '/api/development/methodology/batch',
    method: 'delete',
    data: ids
  })
}

// ==================== 项目管理 API ====================

/**
 * 分页查询开发项目列表
 */
export function getProjectList(params) {
  return request({
    url: '/api/development/projects',
    method: 'get',
    params
  })
}

/**
 * 查询所有项目（不分页，用于下拉选择）
 */
export function getAllProjects(status) {
  return request({
    url: '/api/development/projects/all',
    method: 'get',
    params: { status }
  })
}

/**
 * 根据ID查询项目详情
 */
export function getProjectById(id) {
  return request({
    url: `/api/development/projects/${id}`,
    method: 'get'
  })
}

/**
 * 创建开发项目
 */
export function createProject(data) {
  return request({
    url: '/api/development/projects',
    method: 'post',
    data
  })
}

/**
 * 更新开发项目
 */
export function updateProject(id, data) {
  return request({
    url: `/api/development/projects/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除开发项目
 */
export function deleteProject(id) {
  return request({
    url: `/api/development/projects/${id}`,
    method: 'delete'
  })
}

/**
 * 更新项目阶段
 */
export function updateProjectStep(id, step) {
  return request({
    url: `/api/development/projects/${id}/step`,
    method: 'put',
    params: { step }
  })
}
