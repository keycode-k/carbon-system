import request from '@/utils/request'

/**
 * 获取账户信息
 */
export function getAccountInfo(userId) {
  return request({
    url: `/api/trade/account/info/${userId}`,
    method: 'get'
  })
}

/**
 * 初始化账户
 */
export function initAccount(userId) {
  return request({
    url: '/api/trade/account/init',
    method: 'post',
    params: { userId }
  })
}

/**
 * 充值
 */
export function recharge(userId, amount) {
  return request({
    url: '/api/trade/account/recharge',
    method: 'post',
    params: { userId, amount }
  })
}

/**
 * 提现
 */
export function withdraw(userId, amount) {
  return request({
    url: '/api/trade/account/withdraw',
    method: 'post',
    params: { userId, amount }
  })
}

/**
 * 获取账户统计信息
 */
export function getAccountStatistics(userId) {
  return request({
    url: `/api/trade/account/statistics/${userId}`,
    method: 'get'
  })
}

/**
 * 获取交易订单列表
 */
export function getTradeOrders(params) {
  return request({
    url: '/api/trade/order/list',
    method: 'get',
    params
  })
}

/**
 * 发布交易订单
 */
export function createTradeOrder(data) {
  return request({
    url: '/api/trade/order/create',
    method: 'post',
    data
  })
}

/**
 * 取消订单
 */
export function cancelTradeOrder(orderId) {
  return request({
    url: `/api/trade/order/cancel/${orderId}`,
    method: 'post'
  })
}

/**
 * 查询我的订单
 */
export function getMyOrders(userId, params) {
  return request({
    url: '/api/trade/order/my',
    method: 'get',
    params: { userId, ...params }
  })
}

// ==================== 询报价相关接口 ====================

/**
 * 获取市场询报价单列表
 */
export function getMarketQuotes(params) {
  return request({
    url: '/api/trade/quotes/market',
    method: 'get',
    params
  })
}

/**
 * 获取我的询报价单
 */
export function getMyQuotes(userId, params) {
  return request({
    url: '/api/trade/quotes/my',
    method: 'get',
    params: { userId, ...params }
  })
}

/**
 * 获取询价单详情
 */
export function getQuoteDetail(id) {
  return request({
    url: `/api/trade/quotes/${id}`,
    method: 'get'
  })
}

/**
 * 发布询价单
 */
export function publishQuote(data) {
  return request({
    url: '/api/trade/quotes/publish',
    method: 'post',
    data
  })
}

/**
 * 报价（商家对询价单进行报价）
 */
export function makeQuote(id, finalPrice) {
  return request({
    url: `/api/trade/quotes/${id}/quote`,
    method: 'post',
    params: { finalPrice }
  })
}

/**
 * 接受报价
 */
export function acceptQuote(id, userId) {
  return request({
    url: `/api/trade/quotes/${id}/accept`,
    method: 'post',
    params: { userId }
  })
}

/**
 * 拒绝报价
 */
export function rejectQuote(id, userId) {
  return request({
    url: `/api/trade/quotes/${id}/reject`,
    method: 'post',
    params: { userId }
  })
}

/**
 * 取消询价单
 */
export function cancelQuote(id, userId) {
  return request({
    url: `/api/trade/quotes/${id}/cancel`,
    method: 'post',
    params: { userId }
  })
}

