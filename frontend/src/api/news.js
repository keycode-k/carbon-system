import request from '../utils/request'

/**
 * 新闻资讯API
 */

/**
 * 分页查询新闻列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 当前页码
 * @param {number} params.size - 每页大小
 * @param {string} params.title - 标题关键词（可选）
 * @param {string} params.category - 分类（可选）
 * @param {number} params.status - 状态（可选）
 * @returns {Promise}
 */
export function getNewsList(params) {
  return request({
    url: '/api/news/list',
    method: 'get',
    params
  })
}

/**
 * 查询新闻详情
 * @param {number} id - 新闻ID
 * @returns {Promise}
 */
export function getNewsDetail(id) {
  return request({
    url: `/api/news/${id}`,
    method: 'get'
  })
}

/**
 * 发布新闻
 * @param {Object} data - 新闻数据
 * @returns {Promise}
 */
export function publishNews(data) {
  return request({
    url: '/api/news',
    method: 'post',
    data
  })
}

/**
 * 更新新闻
 * @param {Object} data - 新闻数据
 * @returns {Promise}
 */
export function updateNews(data) {
  return request({
    url: '/api/news',
    method: 'put',
    data
  })
}

/**
 * 删除新闻
 * @param {number} id - 新闻ID
 * @returns {Promise}
 */
export function deleteNews(id) {
  return request({
    url: `/api/news/${id}`,
    method: 'delete'
  })
}

/**
 * 置顶/取消置顶新闻
 * @param {number} id - 新闻ID
 * @param {number} isTop - 是否置顶（0-否 1-是）
 * @returns {Promise}
 */
export function updateNewsTopStatus(id, isTop) {
  return request({
    url: `/api/news/${id}/top`,
    method: 'put',
    params: { isTop }
  })
}

/**
 * 修改新闻状态
 * @param {number} id - 新闻ID
 * @param {number} status - 状态（0-草稿 1-已发布 2-已下线）
 * @returns {Promise}
 */
export function updateNewsStatus(id, status) {
  return request({
    url: `/api/news/${id}/status`,
    method: 'put',
    params: { status }
  })
}
