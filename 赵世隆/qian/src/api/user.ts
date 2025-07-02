import request from '@/utils/request'

// 登录方法
export function loginApi(data: any) {
  return request({
    url: '/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
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

