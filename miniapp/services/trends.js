const request = require('./request.js');

/**
 * 获取动态列表（分页）
 * @param {object} params - 查询参数，如 { pageNum, pageSize }
 */
const getTrendsList = (params = {}) => {
  return request({
    url: '/article/list',
    method: 'GET',
    data: {
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10,
      ...params
    },
    showLoading: true,
  });
};

/**
 * 获取动态详情
 * @param {number} id - 动态ID
 */
const getTrendsDetail = (id) => {
  return request({
    url: `/article/detail/${id}`,
    method: 'GET',
    showLoading: true,
  });
};

/**
 * 搜索动态
 * @param {object} searchParams - 搜索参数
 */
const searchTrends = (searchParams) => {
  return request({
    url: '/article/search',
    method: 'POST',
    data: searchParams,
    showLoading: true,
  });
};

module.exports = {
  getTrendsList,
  getTrendsDetail,
  searchTrends
}; 