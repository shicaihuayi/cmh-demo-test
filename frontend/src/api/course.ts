import request from '@/utils/request';

// 获取课程总数
export function getCourseCount() {
  return request({
    url: '/course/count',
    method: 'get'
  });
}

// 获取课程列表
export function getCourseList(params?: any) {
  const userRole = sessionStorage.getItem('role');
  const isAdmin = userRole === '3';

  // 转换分页参数，兼容不同的后端期望
  const requestParams = {
    ...params,
    // 如果是超级管理员，添加一个明确的标志
    isAdminView: isAdmin,
    // 保留原参数并添加可能的其他分页参数名称
    pageNum: params?.pageNum,
    pageSize: params?.pageSize,
    page: params?.pageNum,
    size: params?.pageSize,
    current: params?.pageNum,
    limit: params?.pageSize
  };
  
  console.log('=== 课程API层参数转换 ===');
  console.log('原始参数:', params);
  console.log('转换后参数:', requestParams);
  console.log('==========================');

  return request({
    url: '/course/list',
    method: 'get',
    params: requestParams
  });
}

// 获取课程详情
export function getCourseDetail(id: any) {
  const userRole = sessionStorage.getItem('role');
  const userId = sessionStorage.getItem('id');
  const isAdmin = userRole === '3';
  
  return request({
    url: `/course/detail?id=${id}&viewerRole=${userRole}&viewerId=${userId}&adminView=${isAdmin}`,
    method: 'get'
  });
}

// 新增课程
export function addCourse(data: any) {
  const userRole = sessionStorage.getItem('role');
  const userId = sessionStorage.getItem('id');
  
  const finalData = {
    ...data,
    authorId: userId,
    // 根据新需求：所有管理员创建的课程都为待审核状态
    status: 'PENDING',
    pass: '待审核', // 保持与现有字段的兼容性
    // 注意：创建者信息（creator, creator_id, department, creator_role）将由后端自动填充
  };

  console.log('向后端发送的新增课程数据:', finalData);
  
  return request({
    url: '/course/add',
    method: 'post',
    data: JSON.stringify(finalData)
  });
}

// 修改课程
export function updateCourse(data: any) {
  const userRole = sessionStorage.getItem('role');
  const userId = sessionStorage.getItem('id');
  const isAdmin = userRole === '3';
  
  return request({
    url: `/course/update?adminEdit=${isAdmin}&operatorRole=${userRole}&operatorId=${userId}`,
    method: 'post',
    data,
    suppressErrorHandler: true
  });
}

// 删除课程
export function deleteCourse(ids: number[]) {
  const userRole = sessionStorage.getItem('role');
  const userId = sessionStorage.getItem('id');
  const isAdmin = userRole === '3';

  // 后端接口只支持单次删除，因此我们为每个ID创建一个删除请求
  const deletePromises = ids.map(id => {
    const deleteData = {
      id: id,
      operatorId: userId ? parseInt(userId, 10) : null,
      operatorRole: userRole ? parseInt(userRole, 10) : null,
      isAdmin: isAdmin
    };
    console.log('发送单门课程删除请求:', deleteData);
    return request({
      url: `/course/delete`,
      method: 'post',
      data: deleteData // axios会自动处理为 application/json
    });
  });

  // 等待所有删除请求完成
  return Promise.all(deletePromises).then(results => {
    // 检查是否有任何请求失败
    const failed = results.filter(res => res.code !== 200);
    if (failed.length > 0) {
      const errorMessages = failed.map(f => f.msg).join('; ');
      // 如果有失败，则整个操作视为失败，并抛出具体的错误信息
      throw new Error(errorMessages || '部分课程删除失败');
    }
    // 所有请求都成功
    return { code: 200, msg: '删除成功' };
  });
}

// 获取待审核课程列表 (用于待办事项)
export function getPendingCourseList(params?: any) {
  return request({
    url: '/course/list',
    method: 'get',
    params: params
  });
} 

// 获取审核列表 (只获取状态为"审核中"的课程)
export function getAuditList(params?: any) {
  return request({
    url: '/course/audit-list',
    method: 'get',
    params: params
  });
}

// 审核通过课程
export function approveCourse(id: number) {
  return request({
    url: `/course/approve/${id}`,
    method: 'post'
  });
}

// 审核驳回课程
export function rejectCourse(id: number) {
  return request({
    url: `/course/reject/${id}`,
    method: 'post'
  });
}

// 课程审核 (兼容现有后端)
export function auditCourse(data: any) {
  return request({
    url: '/course/audit',
    method: 'post',
    data: data
  });
}

// 发布课程 (将状态从"待审核"改为"审核中")
export const publishCourse = (ids: number[]) => {
  const userRole = sessionStorage.getItem('role');
  const userId = sessionStorage.getItem('id');
  
  console.log('=== 课程发布API调试信息 ===');
  console.log('要发布的课程ID列表:', ids);
  console.log('操作者ID:', userId);
  console.log('操作者角色:', userRole);
  console.log('===================');
  
  // 定义多种API调用方式，逐一尝试
  const tryPublishWithDifferentApis = async (id: number) => {
    try {
      console.log(`尝试发布课程 ID: ${id}`);
      const response = await request({
        url: `/course/submitForReview?id=${id}&operatorId=${userId}&operatorRole=${userRole}`,
        method: 'get',
      });
      console.log(`课程发布成功:`, response);
      return response;
    } catch (error) {
      console.error(`课程发布失败:`, error);
      // 尝试使用现有的publish接口
      try {
        const coursesData = [{
          id: id,
          operatorId: userId,
          operatorRole: userRole
        }];
        const fallbackResponse = await request({
          url: '/course/publish',
          method: 'post',
          data: coursesData,
          headers: {
            "Content-Type": "application/json",
          }
        });
        console.log(`课程发布备用接口成功:`, fallbackResponse);
        return fallbackResponse;
      } catch (fallbackError) {
        console.error(`课程发布备用接口也失败:`, fallbackError);
        throw error;
      }
    }
  };
  
  // 处理单个或多个ID
  if (ids.length === 1) {
    return tryPublishWithDifferentApis(ids[0]);
  } else {
    // 多个ID的情况，逐个提交并等待所有完成
    console.log('开始批量发布课程...');
    const promises = ids.map((id, index) => {
      console.log(`批量发布课程[${index + 1}/${ids.length}] ID: ${id}`);
      return tryPublishWithDifferentApis(id).catch(error => {
        console.error(`批量发布课程[${index + 1}]失败:`, error);
        return error;
      });
    });
    
    return Promise.all(promises).then(results => {
      console.log('批量发布课程完成，结果:', results);
      const errors = results.filter(result => result instanceof Error);
      if (errors.length > 0) {
        console.error('批量发布中有错误:', errors);
        throw new Error(`批量发布失败，${errors.length}/${ids.length}个请求失败`);
      }
      return results;
    });
  }
}; 