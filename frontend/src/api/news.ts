import request from '@/utils/request'

// 获取动态列表
export function getNewsList(params: any) {
  const userRole = sessionStorage.getItem('role');
  const userName = sessionStorage.getItem('name');
  // 修正：普通管理员(1)、企业管理员(2)、超级管理员(admin用户名或role=3)都应该有管理员视图
  const isAdmin = userName === 'admin' || userRole === '3' || userRole === '2' || userRole === '1';

  // 转换分页参数，兼容不同的后端期望
  const requestParams = {
    ...params,
    // 如果是管理员，添加一个明确的标志
    isAdminView: isAdmin,
    // 保留原参数
    pageNum: params.pageNum,
    pageSize: params.pageSize,
    // 添加可能的其他分页参数名称
    page: params.pageNum,
    size: params.pageSize,
    current: params.pageNum,
    limit: params.pageSize
  };
  
  console.log('=== API层分页参数转换 ===');
  console.log('原始参数:', params);
  console.log('转换后参数:', requestParams);
  console.log('==========================');
  
  return request({
    url: '/article/list',
    method: 'get',
    params: requestParams
  })
}

// 获取动态详情
export function getNewsDetail(id: any) {
  const userRole = sessionStorage.getItem('role');
  const userName = sessionStorage.getItem('name');
  const userId = sessionStorage.getItem('id');
  const isAdmin = userName === 'admin' || userRole === '3' || userRole === '1';
  
  return request({
    url: `/article/detail?id=${id}&viewerRole=${userRole}&viewerId=${userId}&adminView=${isAdmin}`,
    method: 'get'
  })
}

// 上传图片
export function uploadImg(data: any) {
  return request({
    url: '/file/upload',
    method: 'post',
    data
  })
}

// 新增动态
export function addNews(data: any) {
  const userRole = sessionStorage.getItem('role');
  const userId = sessionStorage.getItem('id');
  
  const finalData = {
    ...data,
    authorId: userId,
    // 根据新需求：所有管理员创建的动态都为待审核状态
    status: 'PENDING'
  };

  console.log('向后端发送的新增数据:', finalData);
  
  return request({
    url: '/article/add',
    method: 'post',
    data: finalData
  })
}

// 修改动态
export function updateNews(data: any) {
  const userRole = sessionStorage.getItem('role');
  const userName = sessionStorage.getItem('name');
  const userId = sessionStorage.getItem('id');
  const isAdmin = userName === 'admin' || userRole === '3' || userRole === '1';
  
  return request({
    url: `/article/update?adminEdit=${isAdmin}&operatorRole=${userRole}&operatorId=${userId}`,
    method: 'post',
    data,
    suppressErrorHandler: true
  })
}

// 管理员强制更新文章 - 新增接口
export function adminUpdateNews(data: any) {
  const userRole = sessionStorage.getItem('role');
  const userId = sessionStorage.getItem('id');
  
  return request({
    url: `/article/adminUpdate?operatorRole=${userRole}&operatorId=${userId}`,
    method: 'post',
    data,
    suppressErrorHandler: true
  })
}

// 删除动态 - 标准删除接口
export function deleteNews(id: any) {
  const userRole = sessionStorage.getItem('role');
  const userName = sessionStorage.getItem('name');
  const userId = sessionStorage.getItem('id');
  const isAdmin = userName === 'admin' || userRole === '3' || userRole === '1';
  const isSuperAdmin = userName === 'admin'; // 超级管理员标识
  
  // 为超级管理员增加更多权限参数
  const deleteData: any = {
    id: id,
    operatorId: userId,
    operatorRole: userRole,
    isAdmin: isAdmin
  };
  
  // 如果是超级管理员，添加额外的权限标识
  if (isSuperAdmin) {
    deleteData.isSuperAdmin = true;
    deleteData.adminForceDelete = true;
    deleteData.override = 'SUPER_ADMIN';
    deleteData.permission = 'DELETE_ANY';
  }
  
  console.log('发送删除请求数据:', deleteData);
  
  return request({
    url: `/article/delete`,
    method: 'post',
    data: deleteData
  })
}

// 超级管理员专用删除接口
export function deleteNewsForAdmin(id: any) {
  const userRole = sessionStorage.getItem('role');
  const userId = sessionStorage.getItem('id');
  
  return request({
    url: `/article/adminDelete`,
    method: 'post',
    data: {
      id: id,
      operatorId: userId,
      operatorRole: userRole,
      adminAction: 'FORCE_DELETE', // 明确标识这是管理员强制删除
      superAdminOverride: true, // 超级管理员覆盖权限
      deletePermission: 'SUPER_ADMIN' // 权限级别标识
    }
  })
}

// 备选删除接口（DELETE方法）- 已废弃，后端不支持
export function deleteNewsWithDelete(id: any) {
  // 这个接口已废弃，因为后端没有对应的DELETE路由
  console.warn('DELETE方法删除接口已废弃，后端不支持此路由');
  return Promise.reject(new Error('DELETE方法删除接口不可用'));
}

// 审核通过
export function approveNews(id: number) {
  return request({
    url: `/article/approve/${id}`,
    method: 'post'
  });
}

// 审核驳回
export function rejectNews(id: number) {
  return request({
    url: `/article/reject/${id}`,
    method: 'post'
  });
}

// 发布动态（普通管理员提交审核）
export const publishNews = (ids: number[]) => {
  const userRole = sessionStorage.getItem('role');
  const userId = sessionStorage.getItem('id');
  
  console.log('=== 发布API调试信息 ===');
  console.log('要发布的ID列表:', ids);
  console.log('操作者ID:', userId);
  console.log('操作者角色:', userRole);
  console.log('===================');
  
  // 使用新的批量发布接口
  return request({
    url: '/article/publish',
    method: 'post',
    data: ids
  });
};

// 获取待审核动态列表
export function getPendingNews() {
  return request({
    url: '/article/pendingList',
    method: 'get'
  });
} 