import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/api/user/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/api/user/register',
    method: 'post',
    data
  })
}

export function getUserInfo(username) {
  return request({
    url: '/api/user/info',
    method: 'get',
    params: { username }
  })
}

export function updateUser(data) {
  return request({
    url: '/api/user/update',
    method: 'post',
    data
  })
}

export function updatePassword(data) {
  return request({
    url: '/api/user/password',
    method: 'post',
    data
  })
}

export function deleteAccount(username) {
  return request({
    url: '/api/user/delete',
    method: 'delete',
    params: { username }
  })
}
