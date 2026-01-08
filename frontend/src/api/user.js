import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} data - { username, password }
 * @returns {Promise} 返回 { token, refreshToken, userId, username, nickname, roles, permissions }
 */
export function login(data) {
  return request({
    url: '/api/user/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 * @param {Object} data - { username, password, email, phone, ... }
 */
export function register(data) {
  return request({
    url: '/api/user/register',
    method: 'post',
    data
  })
}

/**
 * 获取当前登录用户信息
 * 通过请求头中的Authorization携带JWT Token
 * @returns {Promise} 返回 { userId, username, nickname, email, phone, roles, permissions }
 */
export function getUserInfo() {
  return request({
    url: '/api/user/info',
    method: 'get'
  })
}

/**
 * 用户登出
 * 通过请求头中的Authorization携带JWT Token
 */
export function logout() {
  return request({
    url: '/api/user/logout',
    method: 'post'
  })
}

/**
 * 更新用户信息
 * @param {Object} data - { id, nickname, email, phone }
 */
export function updateUser(data) {
  return request({
    url: '/api/user/update',
    method: 'post',
    data
  })
}

/**
 * 修改密码
 * @param {Object} data - { userId, oldPassword, newPassword }
 */
export function updatePassword(data) {
  return request({
    url: '/api/user/password',
    method: 'post',
    data
  })
}

/**
 * 注销账户
 * @param {Number} userId
 */
export function deleteAccount(userId) {
  return request({
    url: '/api/user/delete',
    method: 'delete',
    params: { userId }
  })
}
