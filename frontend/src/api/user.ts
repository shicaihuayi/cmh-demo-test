import request from '@/utils/request'
import axios from 'axios'

// 登录方法
export function login(data: any) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 注册方法
export function registerApi(data: any) {
  return request({
    url: '/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// 获取当前用户信息和登录状态
export function loadMyself() {
  let id = sessionStorage.getItem('id');
  console.log('loadMyself API: 发送请求，用户ID:', id);
  return request({
    url: '/user/findUserById?id=' + id,
    method: 'get'
  });
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

// 修改用户个人信息
export function updateUserProfile(data: any) {
  return request({
    url: '/system/user/profile',
    method: 'put',
    data
  })
}

// 用户密码重置
export function updateUserPwd(oldPassword: any, newPassword: any) {
  const data = {
    oldPassword,
    newPassword
  }
  return request({
    url: '/system/user/profile/updatePwd',
    method: 'put',
    data
  })
}

// 检查登录状态的工具函数
export async function checkLoginStatus(): Promise<{isLogin: boolean, userInfo?: any}> {
  try {
    const response: any = await loadMyself()
    return {
      isLogin: response.isOk || false,
      userInfo: response.isOk ? response.user : null
    }
  } catch (error) {
    console.error('检查登录状态失败:', error)
    return {
      isLogin: false,
      userInfo: null
    }
  }
}

// 获取待办任务列表
export function getTaskList() {
  return request({
    url: '/course/publishList',
    method: 'get'
  });
}

// 获取所有类型的待审核任务统计
export function getAllPendingTasks() {
  return request({
    url: '/task/pending-summary',
    method: 'get'
  });
}

// 分别获取待审核的课程
export function getPendingCourses() {
  return request({
    url: '/course/pendingList',
    method: 'get'
  });
}

// 分别获取待审核的动态
export function getPendingNews() {
  return request({
    url: '/article/pendingList',
    method: 'get'
  });
}

// 分别获取待审核的会议
export function getPendingConferences() {
  return request({
    url: '/confer/pendingList',
    method: 'get'
  });
}

export function getUserList(params?: any) {
  return request({
    url: '/user/list',
    method: 'get',
    params: params
  });
}

// 获取超级管理员用户列表（排除超级管理员role=3）
export function getAdminUserList() {
  return request({
    url: '/user/adminList',
    method: 'get'
  });
}

// 搜索用户
export function searchUsers(data: any) {
  return request({
    url: '/user/search',
    method: 'post',
    data: data
  });
}

// 根据部门ID搜索用户
export function searchUsersByDepartment(departmentId: number) {
  const formData = new FormData();
  formData.append('secId', departmentId.toString());
  return request({
    url: '/user/search1',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

// 根据公司名搜索用户
export function searchUsersByCompany(companyName: string) {
  const formData = new FormData();
  formData.append('companyName', companyName);
  return request({
    url: '/user/searchuserbycompany',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

// 更新用户状态
export function updateUserState(data: any) {
  return request({
    url: '/user/updateState',
    method: 'post',
    data: data
  });
}

// 删除用户
export function deleteUsers(userIds: string[]) {
  return request({
    url: '/user/del',
    method: 'post',
    data: userIds,
    headers: {
      'Content-Type': 'application/json'
    }
  });
}

// 新增用户
export function addUser(data: any) {
  const formData = new FormData();
  formData.append('name', data.name || '');
  formData.append('nickname', data.nickname || '');
  formData.append('pwd', data.pwd || '');
  formData.append('tel', data.tel || '');
  formData.append('email', data.email || '');
  formData.append('section', data.section || '');
  formData.append('secId', data.secId || '');
  formData.append('companyName', data.companyName || '');
  formData.append('sex', data.sex || '');
  formData.append('role', data.role || '');
  formData.append('state', data.state?.toString() || '1');
  formData.append('position', data.position || '');
  
  return request({
    url: '/user/add',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

// 更新用户
export function updateUser(data: any) {
  const formData = new FormData();
  formData.append('id', data.id || '');
  formData.append('name', data.name || '');
  formData.append('nickname', data.nickname || '');
  formData.append('pwd', data.pwd || '');
  formData.append('tel', data.tel || '');
  formData.append('email', data.email || '');
  formData.append('section', data.section || '');
  formData.append('secId', data.secId || '');
  formData.append('companyName', data.companyName || '');
  formData.append('sex', data.sex || '');
  formData.append('role', data.role || '');
  formData.append('position', data.position || '');
  formData.append('state', data.state?.toString() || '1');
  
  return request({
    url: '/user/update',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

// 删除用户（备用方案 - 使用FormData格式）
export function deleteUsersFormData(userIds: string[]) {
  const formData = new FormData();
  userIds.forEach((id, index) => {
    formData.append(`ids[${index}]`, id);
  });
  
  return request({
    url: '/user/del',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}