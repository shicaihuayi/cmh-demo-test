const request = require('./request');

/**
 * 获取会议列表（分页、可分类、可搜索）
 * @param {object} params - 查询参数
 * @param {number} params.pageNum - 当前页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} [params.category] - 会议分类 (可选)
 * @param {string} [params.keyword] - 搜索关键词 (可选)
 */
const getMeetingList = (params) => {
  // 注意：这里的 type 和 keyword 参数是根据旧版 Controller 定义的
  // 旧版Controller可能没有统一处理，所以将 category 和 keyword 映射到 type
  const category = params.category || '';
  const keyword = params.keyword || '';

  // 根据有无keyword，决定调用哪个接口
  let url = '/app/conference/list';
  const data = {
    page: params.pageNum || 1,
    size: params.pageSize || 10,
    category: category,
  };

  if (keyword) {
    url = '/app/conference/search';
    data.keyword = keyword;
    delete data.category; // 搜索接口可能不需要category参数
  }

  return request({
    url: url,
    method: 'GET',
    data: data,
    showLoading: true,
  });
};

/**
 * 根据会议名称获取会议详情
 * @param {string} conferName - 会议名称
 */
const getMeetingDetail = (conferName) => {
  return request({
    url: `/app/conference/detail?conferName=${conferName}`,
    method: 'GET',
    showLoading: false, // 由页面控制loading状态
  });
};

module.exports = {
  getMeetingList,
  getMeetingDetail
};

// 未来可以添加更多接口，例如：
// export const searchMeetings = (keyword) => { ... };
// export const joinMeeting = (id) => { ... }; 