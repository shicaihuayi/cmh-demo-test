const request = require('./request.js');

/**
 * 获取课程列表
 * @param {object} params - 查询参数，如 { pageNum, pageSize }
 */
const getCourseList = (params = {}) => {
  return request({
    url: '/course/list', // 调用实际的课程列表接口
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
 * 获取课程详情
 * @param {number} id - 课程ID
 */
const getCourseDetail = (id) => {
  return request({
    url: `/course/detail/${id}`,
    method: 'GET',
    showLoading: true,
  });
};

/**
 * 搜索课程
 * @param {object} params - 搜索参数
 */
const searchCourses = (params) => {
  return request({
    url: '/course/search',
    method: 'POST',
    data: params,
    showLoading: true,
  });
};

/**
 * 获取我的课程
 */
const getMyCourses = () => {
  return request({
    url: '/course/my',
    method: 'GET'
  });
};

/**
 * 课程报名
 * @param {number} courseId - 课程ID
 */
const enrollCourse = (courseId) => {
  return request({
    url: '/course/enroll',
    method: 'POST',
    data: { courseId },
    showLoading: true
  });
};

module.exports = {
  getCourseList,
  getCourseDetail,
  searchCourses,
  getMyCourses,
  enrollCourse
}; 