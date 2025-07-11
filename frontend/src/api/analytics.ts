import request from '@/utils/request';

// 数据分析相关API接口

/**
 * 获取会议参与统计数据
 */
export function getConferenceStats() {
  return request({
    url: '/analytics/conference-stats',
    method: 'get'
  });
}

/**
 * 获取课程报名统计数据
 */
export function getCourseEnrollmentStats() {
  return request({
    url: '/analytics/course-enrollment-stats',
    method: 'get'
  });
}

/**
 * 获取用户行为分析数据
 */
export function getUserBehaviorAnalytics() {
  return request({
    url: '/analytics/user-behavior',
    method: 'get'
  });
}

/**
 * 获取时间范围内的会议数据
 * @param startDate 开始日期
 * @param endDate 结束日期
 */
export function getConferenceDataByDateRange(startDate: string, endDate: string) {
  return request({
    url: '/analytics/conference-data',
    method: 'get',
    params: {
      startDate,
      endDate
    }
  });
}

/**
 * 获取时间范围内的课程数据
 * @param startDate 开始日期
 * @param endDate 结束日期
 */
export function getCourseDataByDateRange(startDate: string, endDate: string) {
  return request({
    url: '/analytics/course-data',
    method: 'get',
    params: {
      startDate,
      endDate
    }
  });
}

/**
 * 获取综合数据分析报告
 */
export function getComprehensiveReport() {
  return request({
    url: '/analytics/comprehensive-report',
    method: 'get'
  });
} 