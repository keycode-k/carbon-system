import request from '@/utils/request'

// ========== 角色管理 ==========

/**
 * 分页查询角色列表
 */
export function getRoleList(params) {
  return request({
    url: '/api/system/role/list',
    method: 'get',
    params
  })
}

/**
 * 获取所有启用的角色
 */
export function getAllRoles() {
  return request({
    url: '/api/system/role/all',
    method: 'get'
  })
}

/**
 * 获取角色详情
 */
export function getRoleById(id) {
  return request({
    url: `/api/system/role/${id}`,
    method: 'get'
  })
}

/**
 * 创建角色
 */
export function createRole(data) {
  return request({
    url: '/api/system/role',
    method: 'post',
    data
  })
}

/**
 * 更新角色
 */
export function updateRole(id, data) {
  return request({
    url: `/api/system/role/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除角色
 */
export function deleteRole(id) {
  return request({
    url: `/api/system/role/${id}`,
    method: 'delete'
  })
}

/**
 * 获取角色的菜单权限ID列表
 */
export function getRoleMenus(roleId) {
  return request({
    url: `/api/system/role/${roleId}/menus`,
    method: 'get'
  })
}

/**
 * 为角色分配菜单权限
 */
export function assignRoleMenus(roleId, menuIds) {
  return request({
    url: `/api/system/role/${roleId}/menus`,
    method: 'post',
    data: menuIds
  })
}

/**
 * 获取用户的角色列表
 */
export function getUserRoles(userId) {
  return request({
    url: `/api/system/role/user/${userId}`,
    method: 'get'
  })
}

/**
 * 为用户分配角色
 */
export function assignUserRoles(data) {
  return request({
    url: `/api/system/user/roles`,
    method: 'post',
    data
  })
}

/**
 * 获取用户的角色ID列表
 */
export function getUserRoleIds(userId) {
  return request({
    url: `/api/system/user/${userId}/roleIds`,
    method: 'get'
  })
}

// ========== 用户管理 ==========

/**
 * 分页查询用户列表（管理员接口）
 */
export function getUserList(params) {
  return request({
    url: '/api/system/user/list',
    method: 'get',
    params
  })
}

/**
 * 创建用户（管理员接口）
 */
export function createUser(data) {
  return request({
    url: '/api/system/user',
    method: 'post',
    data
  })
}

/**
 * 更新用户（管理员接口）
 */
export function updateUserAdmin(data) {
  return request({
    url: '/api/system/user',
    method: 'put',
    data
  })
}

/**
 * 删除用户（管理员接口）
 */
export function deleteUserAdmin(userId) {
  return request({
    url: `/api/system/user/${userId}`,
    method: 'delete'
  })
}

/**
 * 重置用户密码（管理员接口）
 */
export function resetUserPassword(data) {
  return request({
    url: '/api/system/user/resetPassword',
    method: 'post',
    data
  })
}

// ========== 菜单管理 ==========

/**
 * 获取菜单树形列表
 */
export function getMenuTree() {
  return request({
    url: '/api/system/menu/tree',
    method: 'get'
  })
}

/**
 * 获取所有菜单（平铺列表）
 */
export function getMenuList() {
  return request({
    url: '/api/system/menu/list',
    method: 'get'
  })
}

/**
 * 获取菜单详情
 */
export function getMenuById(id) {
  return request({
    url: `/api/system/menu/${id}`,
    method: 'get'
  })
}

/**
 * 创建菜单
 */
export function createMenu(data) {
  return request({
    url: '/api/system/menu',
    method: 'post',
    data
  })
}

/**
 * 更新菜单
 */
export function updateMenu(id, data) {
  return request({
    url: `/api/system/menu/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除菜单
 */
export function deleteMenu(id) {
  return request({
    url: `/api/system/menu/${id}`,
    method: 'delete'
  })
}

/**
 * 获取当前用户的路由菜单
 */
export function getRouterMenus(userId) {
  return request({
    url: `/api/system/menu/router/${userId}`,
    method: 'get'
  })
}

/**
 * 获取当前用户的权限标识列表
 */
export function getUserPermissions(userId) {
  return request({
    url: `/api/system/menu/permissions/${userId}`,
    method: 'get'
  })
}

// ========== 字典管理 ==========

/**
 * 分页查询字典类型
 */
export function getDictTypeList(params) {
  return request({
    url: '/api/system/dict/type/list',
    method: 'get',
    params
  })
}

/**
 * 获取所有字典类型
 */
export function getAllDictTypes() {
  return request({
    url: '/api/system/dict/type/all',
    method: 'get'
  })
}

/**
 * 获取字典类型详情
 */
export function getDictTypeById(id) {
  return request({
    url: `/api/system/dict/type/${id}`,
    method: 'get'
  })
}

/**
 * 创建字典类型
 */
export function createDictType(data) {
  return request({
    url: '/api/system/dict/type',
    method: 'post',
    data
  })
}

/**
 * 更新字典类型
 */
export function updateDictType(id, data) {
  return request({
    url: `/api/system/dict/type/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除字典类型
 */
export function deleteDictType(id) {
  return request({
    url: `/api/system/dict/type/${id}`,
    method: 'delete'
  })
}

/**
 * 根据字典类型获取字典数据（常用接口）
 */
export function getDictDataByType(dictType) {
  return request({
    url: `/api/system/dict/data/type/${dictType}`,
    method: 'get'
  })
}

/**
 * 分页查询字典数据
 */
export function getDictDataList(params) {
  return request({
    url: '/api/system/dict/data/list',
    method: 'get',
    params
  })
}

/**
 * 创建字典数据
 */
export function createDictData(data) {
  return request({
    url: '/api/system/dict/data',
    method: 'post',
    data
  })
}

/**
 * 更新字典数据
 */
export function updateDictData(id, data) {
  return request({
    url: `/api/system/dict/data/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除字典数据
 */
export function deleteDictData(id) {
  return request({
    url: `/api/system/dict/data/${id}`,
    method: 'delete'
  })
}

// ========== 系统配置管理 ==========

/**
 * 分页查询配置列表
 */
export function getConfigList(params) {
  return request({
    url: '/api/system/config/list',
    method: 'get',
    params
  })
}

/**
 * 获取配置详情
 */
export function getConfigById(id) {
  return request({
    url: `/api/system/config/${id}`,
    method: 'get'
  })
}

/**
 * 根据配置键名获取配置值
 */
export function getConfigByKey(configKey) {
  return request({
    url: `/api/system/config/key/${configKey}`,
    method: 'get'
  })
}

/**
 * 按分组获取配置列表
 */
export function getConfigByGroup(group) {
  return request({
    url: `/api/system/config/group/${group}`,
    method: 'get'
  })
}

/**
 * 获取所有配置分组
 */
export function getConfigGroups() {
  return request({
    url: '/api/system/config/groups',
    method: 'get'
  })
}

/**
 * 新增配置
 */
export function createConfig(data) {
  return request({
    url: '/api/system/config',
    method: 'post',
    data
  })
}

/**
 * 更新配置
 */
export function updateConfig(data) {
  return request({
    url: '/api/system/config',
    method: 'put',
    data
  })
}

/**
 * 删除配置
 */
export function deleteConfig(id) {
  return request({
    url: `/api/system/config/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除配置
 */
export function batchDeleteConfig(ids) {
  return request({
    url: '/api/system/config/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 刷新配置缓存
 */
export function refreshConfigCache() {
  return request({
    url: '/api/system/config/refresh',
    method: 'post'
  })
}
