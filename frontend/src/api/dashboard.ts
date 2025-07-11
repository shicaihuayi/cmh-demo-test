import request from '@/utils/request';

/**
 * 获取仪表盘所需的所有统计数据（一次性获取）
 * 包括用户数量、课程数量、会议数量、待处理事件数量
 */
export function getDashboardStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  });
}

/**
 * 获取课程总数
 * 尝试从课程列表接口中提取总数信息
 */
export function getCourseCount() {
  // 尝试使用只获取一条记录但返回总数的方式
  return request({
    url: '/course/list',
    method: 'get',
    params: {
      pageNum: 1,
      pageSize: 1,
      countOnly: true // 表示我们只需要计数
    }
  });
}

/**
 * 获取用户总数
 */
export function getUserCount() {
  return request({
    url: '/user/count',
    method: 'get'
  });
}

/**
 * 获取会议总数
 */
export function getConferenceCount() {
  return request({
    url: '/confer/count',
    method: 'get'
  });
}

/**
 * 获取待处理事件总数
 */
export function getPendingTasksCount() {
  return request({
    url: '/task/pending-count',
    method: 'get'
  });
} 