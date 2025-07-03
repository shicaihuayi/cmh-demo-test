import request from '@/utils/request';

// 获取课程列表
export function getCourseList(params?: any) {
  return request({
    url: '/course/list',
    method: 'get',
    params: params
  });
}

// 获取待审核课程列表 (用于待办事项)
export function getPendingCourseList(params?: any) {
    return request({
      url: '/course/list', // 注意: 这里和loadPublish用了同一个后端接口
      method: 'get',
      params: params
    });
  } 