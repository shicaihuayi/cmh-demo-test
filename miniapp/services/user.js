// services/user.js
// 用户相关API

const request = require('./request.js')

// 微信登录（小程序专用）
const wxLogin = (loginData) => {
  return request({
    url: '/app/user/wxLogin',
    method: 'POST',
    data: loginData,
    showLoading: true
  })
}

// 手机号密码登录
const phoneLogin = (loginData) => {
  return request({
    url: '/app/user/login',
    method: 'POST',
    data: loginData,
    showLoading: true
  })
}

// 获取用户信息
const getUserInfo = () => {
  const userInfo = wx.getStorageSync('userInfo');
  if (!userInfo || !userInfo.id) {
    return Promise.reject({ msg: '用户未登录' });
  }
  
  return request({
    url: `/app/user/profile?id=${userInfo.id}`,
    method: 'GET'
  }).then(response => {
    // 检查返回的数据格式
    if (response && response.isOk && response.user) {
      return response.user; // 返回用户信息对象
    } else {
      throw new Error(response.msg || '获取用户信息失败');
    }
  });
}

// 更新用户信息
const updateUserInfo = (userInfo) => {
  return request({
    url: '/app/user/updateProfile',
    method: 'POST',
    data: userInfo
  })
}

// 修改密码
const changePassword = (passwordData) => {
  return request({
    url: '/app/user/changePassword',
    method: 'POST',
    data: passwordData
  })
}

// 注册
const register = (userData) => {
  return request({
    url: '/app/user/register',
    method: 'POST',
    data: userData,
    showLoading: true
  })
}

// 发送验证码
const sendVerifyCode = (tel) => {
  return request({
    url: '/app/user/sendCode',
    method: 'POST',
    data: { tel }
  })
}

module.exports = {
  wxLogin,
  phoneLogin,
  getUserInfo,
  updateUserInfo,
  changePassword,
  register,
  sendVerifyCode
} 